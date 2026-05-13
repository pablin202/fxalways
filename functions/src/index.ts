import { initializeApp } from "firebase-admin/app";
import { getFirestore, Timestamp } from "firebase-admin/firestore";
import { onRequest } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";

initializeApp();

const db = getFirestore();
const region = "us-central1";
const frankfurterBaseUrl = process.env.FRANKFURTER_BASE_URL ?? "https://api.frankfurter.dev/v2";
const marketauxApiKey = process.env.MARKETAUX_API_KEY ?? "";
const supportedBases = ["USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "BRL", "MXN", "NZD", "SGD"];
const warmPairs = [
  ["USD", "EUR"],
  ["EUR", "USD"],
  ["USD", "JPY"],
  ["GBP", "USD"],
  ["AUD", "USD"],
  ["USD", "BRL"],
];

type FrankfurterRate = {
  date: string;
  base: string;
  quote: string;
  rate: number;
};

type LatestRatesResponse = {
  base: string;
  date: string;
  rates: Array<{ code: string; value: number }>;
  provider: string;
  refreshedAt: string;
};

type HistoricalResponse = {
  base: string;
  quote: string;
  points: Array<{ date: string; value: number }>;
  provider: string;
};

type NewsImpact = "high" | "med" | "low";
type NewsSentiment = "bullish" | "neutral" | "bearish";

type NewsItem = {
  id: string;
  tag: string;
  impact: NewsImpact;
  title: string;
  summary: string;
  source: string;
  sourceUrl: string;
  publishedAt: string;
  ageLabel: string;
  language: string;
  countries: string[];
  currencies: string[];
  topics: string[];
  sentiment: NewsSentiment;
  moves: Array<{ code: string; change: number }>;
};

type NewsFeedResponse = {
  feedKey: string;
  language: string;
  region: string;
  currencies: string[];
  provider: string;
  refreshedAt: string;
  sentiment: {
    bullish: number;
    neutral: number;
    bearish: number;
  };
  items: NewsItem[];
};

type GdeltArticle = {
  url?: string;
  title?: string;
  seendate?: string;
  socialimage?: string;
  domain?: string;
  sourcecountry?: string;
  language?: string;
};

type GdeltResponse = {
  articles?: GdeltArticle[];
};

type MarketauxEntity = {
  symbol?: string;
  sentiment_score?: number;
};

type MarketauxArticle = {
  uuid?: string;
  title?: string;
  description?: string;
  url?: string;
  source?: string;
  published_at?: string;
  language?: string;
  entities?: MarketauxEntity[];
};

type MarketauxResponse = {
  data?: MarketauxArticle[];
};

export const latestRates = onRequest({ region, cors: true }, async (request, response) => {
  try {
    const base = normalizeCurrency(request.query.base, "USD");
    const payload = await getLatestRates(base);
    response.status(200).json(payload);
  } catch (error) {
    response.status(500).json({ message: errorMessage(error) });
  }
});

export const historicalRates = onRequest({ region, cors: true }, async (request, response) => {
  try {
    const base = normalizeCurrency(request.query.base, "USD");
    const quote = normalizeCurrency(request.query.quote, "EUR");
    const days = normalizeDays(request.query.days);
    const payload = await getHistoricalRates(base, quote, days);
    response.status(200).json(payload);
  } catch (error) {
    response.status(500).json({ message: errorMessage(error) });
  }
});

export const newsFeed = onRequest({ region, cors: true }, async (request, response) => {
  try {
    const language = normalizeLanguage(request.query.language);
    const regionCode = normalizeRegion(request.query.region);
    const currencies = normalizeCurrencyList(request.query.currencies);
    const payload = await getNewsFeed(language, regionCode, currencies);
    response.status(200).json(payload);
  } catch (error) {
    response.status(500).json({ message: errorMessage(error) });
  }
});

export const refreshFxCache = onSchedule(
  {
    region,
    schedule: "every 60 minutes",
    timeZone: "Etc/UTC",
  },
  async () => {
    await Promise.all(supportedBases.map((base) => getLatestRates(base, true)));
    await Promise.all(warmPairs.map(([base, quote]) => getHistoricalRates(base, quote, 365, true)));
  },
);

export const refreshNewsCache = onSchedule(
  {
    region,
    schedule: "every 15 minutes",
    timeZone: "Etc/UTC",
  },
  async () => {
    await Promise.all([
      getNewsFeed("en", "US", ["USD", "EUR", "JPY", "GBP", "BTC"], true),
      getNewsFeed("es", "AR", ["USD", "EUR", "BRL", "BTC"], true),
      getNewsFeed("pt", "BR", ["USD", "BRL", "EUR", "BTC"], true),
    ]);
  },
);

async function getLatestRates(base: string, forceRefresh = false): Promise<LatestRatesResponse> {
  const ref = db.collection("fx_latest").doc(base);
  const cached = await ref.get();
  const cachedData = cached.data() as (LatestRatesResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  const url = new URL(`${frankfurterBaseUrl}/rates`);
  url.searchParams.set("base", base);

  const upstream = await fetchJson<FrankfurterRate[]>(url);
  const first = upstream[0];
  const payload: LatestRatesResponse = {
    base: first?.base ?? base,
    date: first?.date ?? formatDate(new Date()),
    rates: upstream
      .map((rate) => ({ code: rate.quote, value: rate.rate }))
      .sort((left, right) => left.code.localeCompare(right.code)),
    provider: "Frankfurter / European Central Bank",
    refreshedAt: new Date().toISOString(),
  };

  await ref.set({
    ...payload,
    expiresAt: Timestamp.fromMillis(Date.now() + 55 * 60 * 1000),
  });

  return payload;
}

async function getHistoricalRates(
  base: string,
  quote: string,
  days: number,
  forceRefresh = false,
): Promise<HistoricalResponse> {
  const docId = `${base}_${quote}_${days}`;
  const ref = db.collection("fx_history").doc(docId);
  const cached = await ref.get();
  const cachedData = cached.data() as (HistoricalResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return normalizeHistoricalResponse(stripExpiry(cachedData));
  }

  const end = new Date();
  const start = new Date(end);
  start.setUTCDate(start.getUTCDate() - days);

  const url = new URL(`${frankfurterBaseUrl}/rates`);
  url.searchParams.set("base", base);
  url.searchParams.set("quotes", quote);
  url.searchParams.set("from", formatDate(start));
  url.searchParams.set("to", formatDate(end));

  const upstream = await fetchJson<FrankfurterRate[]>(url);
  const pointsByDate = new Map<string, number>();
  for (const rate of upstream) {
    pointsByDate.set(rate.date, rate.rate);
  }

  const payload: HistoricalResponse = {
    base,
    quote,
    points: Array.from(pointsByDate.entries())
      .map(([date, value]) => ({ date, value }))
      .sort((left, right) => left.date.localeCompare(right.date)),
    provider: "Frankfurter / European Central Bank",
  };

  await ref.set({
    ...payload,
    expiresAt: Timestamp.fromMillis(Date.now() + 6 * 60 * 60 * 1000),
  });

  return payload;
}

function normalizeHistoricalResponse(payload: HistoricalResponse): HistoricalResponse {
  const pointsByDate = new Map<string, number>();
  for (const point of payload.points) {
    pointsByDate.set(point.date, point.value);
  }

  return {
    ...payload,
    points: Array.from(pointsByDate.entries())
      .map(([date, value]) => ({ date, value }))
      .sort((left, right) => left.date.localeCompare(right.date)),
  };
}

async function getNewsFeed(
  language: string,
  regionCode: string,
  currencies: string[],
  forceRefresh = false,
): Promise<NewsFeedResponse> {
  const feedKey = `${language}_${regionCode}_${currencies.join("-")}`;
  const ref = db.collection("fx_news_feeds").doc(feedKey);
  const cached = await ref.get();
  const cachedData = cached.data() as (NewsFeedResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  let payload: NewsFeedResponse;
  try {
    payload = marketauxApiKey
      ? await fetchMarketauxNews(language, regionCode, currencies, feedKey)
      : await fetchGdeltNews(language, regionCode, currencies, feedKey);
  } catch (error) {
    const stale = cachedData ? stripExpiry(cachedData) : null;
    payload = stale ?? fallbackNewsFeed(language, regionCode, currencies, feedKey);
  }

  await ref.set({
    ...payload,
    expiresAt: Timestamp.fromMillis(Date.now() + (payload.provider === "fallback" ? 90 * 1000 : 15 * 60 * 1000)),
  });

  return payload;
}

async function fetchMarketauxNews(
  language: string,
  regionCode: string,
  currencies: string[],
  feedKey: string,
): Promise<NewsFeedResponse> {
  const url = new URL("https://api.marketaux.com/v1/news/all");
  url.searchParams.set("api_token", marketauxApiKey);
  url.searchParams.set("language", language);
  url.searchParams.set("countries", regionCode.toLowerCase());
  url.searchParams.set("symbols", currencies.join(","));
  url.searchParams.set("filter_entities", "true");
  url.searchParams.set("limit", "10");

  const upstream = await fetchJson<MarketauxResponse>(url);
  const items = (upstream.data ?? []).slice(0, 8).map((article, index) => {
    const entitySentiment = article.entities?.find((entity) => typeof entity.sentiment_score === "number")?.sentiment_score ?? 0;
    const detectedCurrencies = detectCurrencies(`${article.title ?? ""} ${article.description ?? ""}`, currencies);
    return buildNewsItem({
      id: article.uuid ?? stableId(article.url ?? `${feedKey}-${index}`),
      title: article.title ?? "Market update",
      summary: article.description ?? "Latest market development affecting currency pairs.",
      source: article.source ?? domainFromUrl(article.url ?? ""),
      sourceUrl: article.url ?? "",
      publishedAt: article.published_at ?? new Date().toISOString(),
      language: article.language ?? language,
      regionCode,
      currencies: detectedCurrencies.length > 0 ? detectedCurrencies : currencies.slice(0, 2),
      sentimentHint: entitySentiment,
    });
  });

  return assembleNewsFeed(feedKey, language, regionCode, currencies, "Marketaux", items);
}

async function fetchGdeltNews(
  language: string,
  regionCode: string,
  currencies: string[],
  feedKey: string,
): Promise<NewsFeedResponse> {
  const currencyTerms = currencies.flatMap(currencyNewsTerms).slice(0, 18);
  const query = Array.from(new Set([
    ...currencyTerms,
    "inflation",
    "interest rates",
    "currency markets",
  ])).slice(0, 12).join(" OR ");

  const url = new URL("https://api.gdeltproject.org/api/v2/doc/doc");
  url.searchParams.set("query", query);
  url.searchParams.set("mode", "ArtList");
  url.searchParams.set("format", "json");
  url.searchParams.set("maxrecords", "12");
  url.searchParams.set("sort", "HybridRel");

  const upstream = await fetchJson<GdeltResponse>(url);
  const items = (upstream.articles ?? []).slice(0, 8).map((article, index) => {
    const title = article.title ?? "Currency market update";
    const detectedCurrencies = detectCurrencies(title, currencies);
    return buildNewsItem({
      id: stableId(article.url ?? `${feedKey}-${index}`),
      title,
      summary: summarizeTitle(title, detectedCurrencies),
      source: article.domain ?? domainFromUrl(article.url ?? ""),
      sourceUrl: article.url ?? "",
      publishedAt: parseGdeltDate(article.seendate),
      language: article.language ?? language,
      regionCode,
      currencies: detectedCurrencies.length > 0 ? detectedCurrencies : currencies.slice(0, 2),
      sentimentHint: inferSentimentScore(title),
    });
  });

  return assembleNewsFeed(feedKey, language, regionCode, currencies, "GDELT", items);
}

function assembleNewsFeed(
  feedKey: string,
  language: string,
  regionCode: string,
  currencies: string[],
  provider: string,
  items: NewsItem[],
): NewsFeedResponse {
  const resolvedItems = items.length > 0 ? items : fallbackNewsFeed(language, regionCode, currencies, feedKey).items;
  const bullish = resolvedItems.filter((item) => item.sentiment === "bullish").length;
  const bearish = resolvedItems.filter((item) => item.sentiment === "bearish").length;
  const neutral = Math.max(resolvedItems.length - bullish - bearish, 0);
  const total = Math.max(resolvedItems.length, 1);

  return {
    feedKey,
    language,
    region: regionCode,
    currencies,
    provider,
    refreshedAt: new Date().toISOString(),
    sentiment: {
      bullish: Math.round((bullish / total) * 100),
      neutral: Math.round((neutral / total) * 100),
      bearish: Math.round((bearish / total) * 100),
    },
    items: resolvedItems,
  };
}

function buildNewsItem(input: {
  id: string;
  title: string;
  summary: string;
  source: string;
  sourceUrl: string;
  publishedAt: string;
  language: string;
  regionCode: string;
  currencies: string[];
  sentimentHint: number;
}): NewsItem {
  const sentiment: NewsSentiment = input.sentimentHint > 0.08 ? "bullish" : input.sentimentHint < -0.08 ? "bearish" : "neutral";
  const impact: NewsImpact = isHighImpact(input.title) ? "high" : input.currencies.length > 1 ? "med" : "low";
  const primary = input.currencies[0] ?? "USD";
  const change = sentiment === "bullish" ? 0.34 : sentiment === "bearish" ? -0.34 : 0.0;

  return {
    id: input.id,
    tag: tagForTitle(input.title, primary),
    impact,
    title: input.title,
    summary: input.summary,
    source: input.source || "Market source",
    sourceUrl: input.sourceUrl,
    publishedAt: input.publishedAt,
    ageLabel: ageLabel(input.publishedAt),
    language: input.language,
    countries: [input.regionCode],
    currencies: input.currencies,
    topics: topicsForTitle(input.title),
    sentiment,
    moves: input.currencies.slice(0, 3).map((code, index) => ({
      code,
      change: index === 0 ? change : Number((change * 0.42).toFixed(2)),
    })),
  };
}

function fallbackNewsFeed(language: string, regionCode: string, currencies: string[], feedKey: string): NewsFeedResponse {
  const now = new Date().toISOString();
  const copy = fallbackNewsCopy(language);
  const items: NewsItem[] = [
    buildNewsItem({
      id: `${feedKey}-macro`,
      title: copy.macroTitle,
      summary: copy.macroSummary,
      source: "FX Always",
      sourceUrl: "",
      publishedAt: now,
      language,
      regionCode,
      currencies: currencies.slice(0, 3),
      sentimentHint: 0,
    }),
    buildNewsItem({
      id: `${feedKey}-inflation`,
      title: copy.inflationTitle,
      summary: copy.inflationSummary,
      source: "FX Always",
      sourceUrl: "",
      publishedAt: now,
      language,
      regionCode,
      currencies: ["USD", ...currencies.filter((code) => code !== "USD")].slice(0, 3),
      sentimentHint: -0.1,
    }),
  ];
  return assembleNewsFeed(feedKey, language, regionCode, currencies, "fallback", items);
}

function fallbackNewsCopy(language: string): {
  macroTitle: string;
  macroSummary: string;
  inflationTitle: string;
  inflationSummary: string;
} {
  const copy: Record<string, ReturnType<typeof fallbackNewsCopy>> = {
    en: {
      macroTitle: "Central bank guidance keeps currency markets range-bound",
      macroSummary: "Rates traders are watching inflation and policy commentary for the next move across major FX pairs.",
      inflationTitle: "Inflation data remains the key catalyst for dollar pairs",
      inflationSummary: "Upcoming CPI and labor-market readings can shift rate expectations and impact USD crosses.",
    },
    es: {
      macroTitle: "Los bancos centrales mantienen a las divisas en rango",
      macroSummary: "El mercado sigue la inflacion y los mensajes de politica monetaria para anticipar el proximo movimiento.",
      inflationTitle: "La inflacion sigue siendo el catalizador clave del dolar",
      inflationSummary: "Los proximos datos de precios y empleo pueden mover expectativas de tasas y cruces contra USD.",
    },
    pt: {
      macroTitle: "Bancos centrais mantem moedas em faixa lateral",
      macroSummary: "Operadores acompanham inflacao e comentarios de politica monetaria para o proximo movimento do FX.",
      inflationTitle: "Inflacao continua sendo o principal catalisador do dolar",
      inflationSummary: "Dados de precos e mercado de trabalho podem mudar expectativas de juros e pares com USD.",
    },
    zh: {
      macroTitle: "央行指引让汇市维持区间震荡",
      macroSummary: "交易员关注通胀和政策表态，以判断主要货币对的下一步方向。",
      inflationTitle: "通胀数据仍是美元货币对的关键催化剂",
      inflationSummary: "即将公布的物价和就业数据可能改变利率预期并影响美元交叉盘。",
    },
    hi: {
      macroTitle: "केंद्रीय बैंक संकेतों से मुद्रा बाजार सीमित दायरे में",
      macroSummary: "ट्रेडर प्रमुख FX जोड़ों की अगली चाल के लिए महंगाई और नीति टिप्पणी देख रहे हैं।",
      inflationTitle: "डॉलर जोड़ों के लिए महंगाई आंकड़े मुख्य संकेतक बने हुए हैं",
      inflationSummary: "आने वाले CPI और रोजगार आंकड़े दर अपेक्षाओं तथा USD क्रॉस को प्रभावित कर सकते हैं।",
    },
    fr: {
      macroTitle: "Les banques centrales gardent les devises dans une fourchette",
      macroSummary: "Les cambistes suivent l'inflation et les messages de politique monetaire pour le prochain mouvement.",
      inflationTitle: "L'inflation reste le catalyseur cle des paires en dollar",
      inflationSummary: "Les prochaines donnees de prix et d'emploi peuvent modifier les attentes de taux et les croisements USD.",
    },
    ar: {
      macroTitle: "توجيهات البنوك المركزية تبقي العملات ضمن نطاق محدود",
      macroSummary: "يراقب المتداولون التضخم وتعليقات السياسة النقدية لتوقع الحركة التالية في أزواج العملات الرئيسية.",
      inflationTitle: "بيانات التضخم تبقى المحرك الأهم لأزواج الدولار",
      inflationSummary: "قراءات الأسعار والعمل قد تغير توقعات الفائدة وتؤثر في تقاطعات الدولار.",
    },
    bn: {
      macroTitle: "কেন্দ্রীয় ব্যাংকের বার্তায় মুদ্রাবাজার সীমিত দোলাচলে",
      macroSummary: "প্রধান FX জোড়ার পরবর্তী দিক বুঝতে ট্রেডাররা মূল্যস্ফীতি ও নীতি মন্তব্য দেখছেন।",
      inflationTitle: "ডলার জোড়ার মূল চালিকা শক্তি এখনও মূল্যস্ফীতি",
      inflationSummary: "আসন্ন CPI ও শ্রমবাজার তথ্য সুদের প্রত্যাশা এবং USD ক্রস বদলাতে পারে।",
    },
    ru: {
      macroTitle: "Сигналы центробанков удерживают валюты в диапазоне",
      macroSummary: "Трейдеры следят за инфляцией и комментариями по политике для следующего движения FX-пар.",
      inflationTitle: "Инфляция остается ключевым драйвером долларовых пар",
      inflationSummary: "Данные по ценам и занятости могут изменить ожидания по ставкам и кроссы с USD.",
    },
    ur: {
      macroTitle: "مرکزی بینکوں کی رہنمائی سے کرنسی مارکیٹ محدود دائرے میں",
      macroSummary: "ٹریڈرز اہم FX pairs کی اگلی سمت کے لیے افراط زر اور پالیسی تبصروں کو دیکھ رہے ہیں۔",
      inflationTitle: "ڈالر pairs کے لیے inflation data اہم محرک ہے",
      inflationSummary: "آنے والے CPI اور labor readings شرح توقعات اور USD crosses کو بدل سکتے ہیں۔",
    },
    id: {
      macroTitle: "Arahan bank sentral membuat pasar mata uang bergerak terbatas",
      macroSummary: "Trader memantau inflasi dan komentar kebijakan untuk arah berikutnya pada pair FX utama.",
      inflationTitle: "Data inflasi tetap menjadi katalis utama pair dolar",
      inflationSummary: "Data CPI dan tenaga kerja berikutnya dapat mengubah ekspektasi suku bunga dan cross USD.",
    },
    de: {
      macroTitle: "Notenbank-Signale halten Devisen in einer Spanne",
      macroSummary: "Trader beobachten Inflation und geldpolitische Kommentare fur den nachsten Schritt wichtiger FX-Paare.",
      inflationTitle: "Inflationsdaten bleiben der wichtigste Treiber fur Dollar-Paare",
      inflationSummary: "Neue Preis- und Arbeitsmarktdaten konnen Zinserwartungen und USD-Kreuze bewegen.",
    },
    ja: {
      macroTitle: "中央銀行の見通しで為替市場はレンジ推移",
      macroSummary: "主要FXペアの次の動きを見るため、トレーダーはインフレと政策発言を注視しています。",
      inflationTitle: "インフレ指標はドルペアの主要材料です",
      inflationSummary: "今後のCPIと雇用関連データは金利期待とUSDクロスに影響する可能性があります。",
    },
  };
  return copy[language] ?? copy.en;
}

function normalizeLanguage(value: unknown): string {
  const raw = Array.isArray(value) ? value[0] : value;
  if (typeof raw !== "string") return "en";
  const language = raw.trim().toLowerCase().slice(0, 2);
  return /^[a-z]{2}$/.test(language) ? language : "en";
}

function normalizeRegion(value: unknown): string {
  const raw = Array.isArray(value) ? value[0] : value;
  if (typeof raw !== "string") return "US";
  const regionValue = raw.trim().toUpperCase().slice(0, 2);
  return /^[A-Z]{2}$/.test(regionValue) ? regionValue : "US";
}

function normalizeCurrencyList(value: unknown): string[] {
  const raw = Array.isArray(value) ? value[0] : value;
  if (typeof raw !== "string") return ["USD", "EUR", "JPY", "GBP", "BTC"];
  const parsed = raw
    .split(",")
    .map((item) => item.trim().toUpperCase())
    .filter((item) => /^[A-Z]{3,5}$/.test(item));
  return Array.from(new Set(parsed.length > 0 ? parsed : ["USD", "EUR", "JPY", "GBP", "BTC"])).slice(0, 8);
}

function detectCurrencies(text: string, preferred: string[]): string[] {
  const upper = text.toUpperCase();
  return preferred.filter((code) => upper.includes(code) || currencyNewsTerms(code).some((term) => upper.includes(term.toUpperCase())));
}

function currencyNewsTerms(code: string): string[] {
  switch (code) {
    case "USD": return ["USD", "dollar", "Federal Reserve", "Fed"];
    case "EUR": return ["EUR", "euro", "ECB", "Eurozone"];
    case "JPY": return ["JPY", "yen", "Bank of Japan", "BoJ"];
    case "GBP": return ["GBP", "sterling", "Bank of England"];
    case "BRL": return ["BRL", "real", "Brazil central bank"];
    case "MXN": return ["MXN", "peso", "Banxico"];
    case "AUD": return ["AUD", "Australian dollar", "RBA"];
    case "CAD": return ["CAD", "Canadian dollar", "Bank of Canada"];
    case "BTC": return ["BTC", "Bitcoin", "crypto"];
    case "ETH": return ["ETH", "Ethereum", "crypto"];
    default: return [code];
  }
}

function summarizeTitle(title: string, currencies: string[]): string {
  const target = currencies.length > 0 ? currencies.join("/") : "major FX pairs";
  return `This story is relevant to ${target}; monitor rate expectations, risk appetite, and liquidity conditions.`;
}

function inferSentimentScore(title: string): number {
  const lower = title.toLowerCase();
  if (/(rise|rises|gain|gains|strong|surge|rally|hawkish|higher)/.test(lower)) return 0.18;
  if (/(fall|falls|drop|drops|weak|slump|dovish|lower|risk-off)/.test(lower)) return -0.18;
  return 0;
}

function isHighImpact(title: string): boolean {
  return /(central bank|federal reserve|ecb|boj|inflation|cpi|jobs|payroll|rates?|policy)/i.test(title);
}

function topicsForTitle(title: string): string[] {
  const topics: string[] = [];
  if (/inflation|cpi/i.test(title)) topics.push("inflation");
  if (/central bank|federal reserve|ecb|boj|rates?|policy/i.test(title)) topics.push("central_bank");
  if (/crypto|bitcoin|ethereum/i.test(title)) topics.push("crypto");
  if (topics.length === 0) topics.push("macro");
  return topics;
}

function tagForTitle(title: string, fallback: string): string {
  if (/ecb|eurozone/i.test(title)) return "ECB";
  if (/federal reserve|fed/i.test(title)) return "FED";
  if (/boj|bank of japan|yen/i.test(title)) return "BOJ";
  if (/inflation|cpi/i.test(title)) return "CPI";
  if (/bitcoin|crypto/i.test(title)) return "BTC";
  return fallback;
}

function ageLabel(publishedAt: string): string {
  const timestamp = Date.parse(publishedAt);
  if (!Number.isFinite(timestamp)) return "now";
  const minutes = Math.max(Math.round((Date.now() - timestamp) / 60000), 0);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.round(hours / 24)}d ago`;
}

function parseGdeltDate(value: string | undefined): string {
  if (!value) return new Date().toISOString();
  const compact = value.replace(/\D/g, "");
  if (compact.length >= 14) {
    return `${compact.slice(0, 4)}-${compact.slice(4, 6)}-${compact.slice(6, 8)}T${compact.slice(8, 10)}:${compact.slice(10, 12)}:${compact.slice(12, 14)}Z`;
  }
  return new Date().toISOString();
}

function domainFromUrl(value: string): string {
  try {
    return new URL(value).hostname.replace(/^www\./, "");
  } catch {
    return "";
  }
}

function stableId(value: string): string {
  let hash = 0;
  for (let index = 0; index < value.length; index++) {
    hash = (hash * 31 + value.charCodeAt(index)) | 0;
  }
  return `n_${Math.abs(hash)}`;
}

async function fetchJson<T>(url: URL): Promise<T> {
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Provider failed with ${response.status}`);
  }
  return response.json() as Promise<T>;
}

function normalizeCurrency(value: unknown, fallback: string): string {
  const raw = Array.isArray(value) ? value[0] : value;
  if (typeof raw !== "string") return fallback;
  const currency = raw.trim().toUpperCase();
  return /^[A-Z]{3}$/.test(currency) ? currency : fallback;
}

function normalizeDays(value: unknown): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = typeof raw === "string" ? Number.parseInt(raw, 10) : 365;
  if (!Number.isFinite(parsed)) return 365;
  return Math.min(Math.max(parsed, 7), 1825);
}

function formatDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

function stripExpiry<T extends { expiresAt?: Timestamp }>(value: T): Omit<T, "expiresAt"> {
  const { expiresAt: _, ...rest } = value;
  return rest;
}

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Unknown error";
}
