import { initializeApp } from "firebase-admin/app";
import { getFirestore, QueryDocumentSnapshot, Timestamp } from "firebase-admin/firestore";
import { getMessaging, MulticastMessage } from "firebase-admin/messaging";
import { defineSecret } from "firebase-functions/params";
import { onRequest } from "firebase-functions/v2/https";
import { onSchedule } from "firebase-functions/v2/scheduler";

initializeApp();

const db = getFirestore();
const messaging = getMessaging();
const region = "us-central1";
const frankfurterBaseUrl = process.env.FRANKFURTER_BASE_URL ?? "https://api.frankfurter.dev/v2";
const coinPaprikaBaseUrl = process.env.COINPAPRIKA_BASE_URL ?? "https://api.coinpaprika.com/v1";
const exchangeRateApiKey = process.env.EXCHANGE_RATE_API_KEY ?? "";
const marketauxApiKey = defineSecret("MARKETAUX_API_KEY");
const SERVER_ALERT_COOLDOWN_MILLIS = 6 * 60 * 60 * 1000;
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

type CurrencyInfo = {
  code: string;
  name: string;
  symbol: string;
  flag: string;
  country: string;
  region: string;
  isPopular: boolean;
};

type SupportedCurrenciesResponse = {
  provider: string;
  refreshedAt: string;
  currencies: CurrencyInfo[];
};

type HistoricalResponse = {
  base: string;
  quote: string;
  points: Array<{ date: string; value: number }>;
  provider: string;
};

type CryptoMarketsResponse = {
  base: string;
  provider: string;
  refreshedAt: string;
  assets: CryptoMarketAsset[];
};

type CryptoMarketAsset = {
  code: string;
  name: string;
  glyph: string;
  kind: "Crypto";
  stable: boolean;
  rank: number | null;
  priceUsd: number;
  priceBase: number;
  value: number;
  change24h: number;
  marketCapUsd: number | null;
  volume24hUsd: number | null;
  sparkline: number[];
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

type PriceAlertDirection = "Above" | "Below";
type PriceAlertKind = "Target" | "DailyChange";

type PriceAlert = {
  id: string;
  base: string;
  quote: string;
  target: number;
  direction: PriceAlertDirection;
  kind?: PriceAlertKind;
  enabled?: boolean;
  createdAtMillis?: number;
  lastTriggeredAtMillis?: number | null;
};

type BackupSettings = {
  themeMode?: string;
  language?: string;
  baseCurrency?: string;
  travelerCurrency?: string;
  travelerBudgetBase?: number;
  converterCurrencyCodes?: string[];
  compareCurrencyCodes?: string[];
  providerPreferenceCodes?: string[];
  userProfile?: string;
};

type UserBackupSnapshot = {
  schemaVersion?: number;
  updatedAtMillis?: number;
  settings?: BackupSettings;
  alerts?: PriceAlert[];
  watchlist?: unknown;
};

type ServerAlertEvent = {
  alertId: string;
  uid: string;
  base: string;
  quote: string;
  kind: PriceAlertKind;
  direction: PriceAlertDirection;
  target: number;
  observedValue: number;
  triggeredAtMillis: number;
  source: "server";
};

type PushTokenDoc = {
  token?: string;
  platform?: string;
  enabled?: boolean;
};

type AlertNotificationCopy = {
  alertHit: string;
  above: string;
  below: string;
  up: string;
  down: string;
  now: string;
  twentyFourHour: string;
};

type ProviderCatalogItem = {
  id: string;
  label: string;
  category: "Transfer provider" | "Wallet / payout" | "Local rail" | "Digital dollar";
  quoteMode: "Live quote ready" | "Partner API required" | "Estimated" | "Wallet only";
  markets: string[];
  currencies: string[];
  quoteCapable: boolean;
  priority: number;
  subtitle: string;
};

type ProviderCatalogResponse = {
  provider: string;
  refreshedAt: string;
  region: string;
  baseCurrency: string;
  primary: ProviderCatalogItem[];
  other: ProviderCatalogItem[];
};

type ProviderQuoteStatus = "live" | "comparison" | "estimated" | "partner_setup" | "unavailable";

type ProviderQuote = {
  providerId: string;
  provider: string;
  status: ProviderQuoteStatus;
  source: string;
  sourceUrl: string;
  amount: number;
  sourceCurrency: string;
  targetCurrency: string;
  receivedAmount: number;
  feeAmount: number;
  markupPercent: number;
  lossAmount: number;
  lossPercent: number;
  effectiveRate: number;
  deliverySpeed: string;
  paymentMethod: string;
  riskLabel: string;
  bestFor: string;
  quoteMode: ProviderCatalogItem["quoteMode"];
  refreshedAt: string;
  expiresAt: string;
  message: string;
};

type ProviderQuotesResponse = {
  provider: string;
  refreshedAt: string;
  base: string;
  target: string;
  amount: number;
  plan: "free" | "pro";
  midMarketRate: number;
  midMarketTarget: number;
  quotes: ProviderQuote[];
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

type ExchangeRateApiLatestResponse = {
  result?: string;
  base_code?: string;
  time_last_update_utc?: string;
  conversion_rates?: Record<string, number>;
  "error-type"?: string;
};

type CoinPaprikaQuote = {
  price?: number;
  volume_24h?: number;
  market_cap?: number;
  percent_change_1h?: number;
  percent_change_6h?: number;
  percent_change_12h?: number;
  percent_change_24h?: number;
};

type CoinPaprikaTicker = {
  id?: string;
  name?: string;
  symbol?: string;
  rank?: number;
  quotes?: {
    USD?: CoinPaprikaQuote;
  };
};

type WiseQuoteResponse = {
  sourceAmount?: number;
  targetAmount?: number;
  rate?: number;
  rateExpirationTime?: string;
  expirationTime?: string;
  paymentOptions?: Array<{
    formattedEstimatedDelivery?: string;
    estimatedDelivery?: string;
    payIn?: string;
    targetAmount?: number;
    disabled?: boolean;
    price?: {
      total?: {
        value?: {
          amount?: number;
          currency?: string;
        };
      };
    };
  }>;
};

type RevolutRateResponse = {
  from?: { amount?: number; currency?: string };
  to?: { amount?: number; currency?: string };
  rate?: number;
  fee?: { amount?: number; currency?: string };
  rate_date?: string;
};

type WiseComparisonResponse = {
  providers?: WiseComparisonProvider[];
};

type WiseComparisonProvider = {
  alias?: string;
  name?: string;
  type?: string;
  quotes?: WiseComparisonQuote[];
};

type WiseComparisonQuote = {
  dateCollected?: string;
  fee?: number;
  markup?: number;
  rate?: number;
  receivedAmount?: number;
  sendAmount?: number | null;
  sourceCountry?: string | null;
  targetCountry?: string | null;
  deliveryEstimation?: {
    providerGivesEstimate?: boolean;
    duration?: {
      min?: string;
      max?: string;
    };
  };
};

function currency(
  code: string,
  name: string,
  symbol = "",
  flag = "◆",
  country = "",
  region = "",
  isPopular = false,
): CurrencyInfo {
  return { code, name, symbol, flag, country, region, isPopular };
}

function providerOption(
  id: string,
  label: string,
  category: ProviderCatalogItem["category"],
  quoteMode: ProviderCatalogItem["quoteMode"],
  markets: string[],
  currencies: string[],
  quoteCapable: boolean,
  priority: number,
  subtitle: string,
): ProviderCatalogItem {
  return { id, label, category, quoteMode, markets, currencies, quoteCapable, priority, subtitle };
}

const currencyCatalog: CurrencyInfo[] = [
  currency("USD", "US Dollar", "$", "🇺🇸", "United States", "North America", true),
  currency("EUR", "Euro", "€", "🇪🇺", "Eurozone", "Europe", true),
  currency("GBP", "British Pound", "£", "🇬🇧", "United Kingdom", "Europe", true),
  currency("JPY", "Japanese Yen", "¥", "🇯🇵", "Japan", "Asia", true),
  currency("AUD", "Australian Dollar", "A$", "🇦🇺", "Australia", "Oceania", true),
  currency("CAD", "Canadian Dollar", "C$", "🇨🇦", "Canada", "North America", true),
  currency("CHF", "Swiss Franc", "Fr", "🇨🇭", "Switzerland", "Europe", true),
  currency("CNY", "Chinese Yuan", "¥", "🇨🇳", "China", "Asia", true),
  currency("BRL", "Brazilian Real", "R$", "🇧🇷", "Brazil", "South America", true),
  currency("MXN", "Mexican Peso", "$", "🇲🇽", "Mexico", "North America", true),
  currency("NZD", "New Zealand Dollar", "NZ$", "🇳🇿", "New Zealand", "Oceania", true),
  currency("SGD", "Singapore Dollar", "S$", "🇸🇬", "Singapore", "Asia", true),
  currency("AED", "UAE Dirham", "د.إ", "🇦🇪", "United Arab Emirates", "Middle East"),
  currency("AFN", "Afghan Afghani", "؋", "🇦🇫", "Afghanistan", "Asia"),
  currency("ALL", "Albanian Lek", "L", "🇦🇱", "Albania", "Europe"),
  currency("AMD", "Armenian Dram", "֏", "🇦🇲", "Armenia", "Asia"),
  currency("ANG", "Netherlands Antillean Guilder", "ƒ", "🇨🇼", "Curacao", "Caribbean"),
  currency("AOA", "Angolan Kwanza", "Kz", "🇦🇴", "Angola", "Africa"),
  currency("ARS", "Argentine Peso", "$", "🇦🇷", "Argentina", "South America"),
  currency("AWG", "Aruban Florin", "ƒ", "🇦🇼", "Aruba", "Caribbean"),
  currency("AZN", "Azerbaijani Manat", "₼", "🇦🇿", "Azerbaijan", "Asia"),
  currency("BAM", "Bosnia-Herzegovina Convertible Mark", "KM", "🇧🇦", "Bosnia and Herzegovina", "Europe"),
  currency("BBD", "Barbadian Dollar", "$", "🇧🇧", "Barbados", "Caribbean"),
  currency("BDT", "Bangladeshi Taka", "৳", "🇧🇩", "Bangladesh", "Asia"),
  currency("BGN", "Bulgarian Lev", "лв", "🇧🇬", "Bulgaria", "Europe"),
  currency("BHD", "Bahraini Dinar", ".د.ب", "🇧🇭", "Bahrain", "Middle East"),
  currency("BIF", "Burundian Franc", "FBu", "🇧🇮", "Burundi", "Africa"),
  currency("BMD", "Bermudian Dollar", "$", "🇧🇲", "Bermuda", "Atlantic"),
  currency("BND", "Brunei Dollar", "B$", "🇧🇳", "Brunei", "Asia"),
  currency("BOB", "Bolivian Boliviano", "Bs", "🇧🇴", "Bolivia", "South America"),
  currency("BSD", "Bahamian Dollar", "$", "🇧🇸", "Bahamas", "Caribbean"),
  currency("BTN", "Bhutanese Ngultrum", "Nu.", "🇧🇹", "Bhutan", "Asia"),
  currency("BWP", "Botswana Pula", "P", "🇧🇼", "Botswana", "Africa"),
  currency("BYN", "Belarusian Ruble", "Br", "🇧🇾", "Belarus", "Europe"),
  currency("BZD", "Belize Dollar", "BZ$", "🇧🇿", "Belize", "Central America"),
  currency("CDF", "Congolese Franc", "FC", "🇨🇩", "Democratic Republic of the Congo", "Africa"),
  currency("CLP", "Chilean Peso", "$", "🇨🇱", "Chile", "South America"),
  currency("COP", "Colombian Peso", "$", "🇨🇴", "Colombia", "South America"),
  currency("CRC", "Costa Rican Colon", "₡", "🇨🇷", "Costa Rica", "Central America"),
  currency("CUP", "Cuban Peso", "$", "🇨🇺", "Cuba", "Caribbean"),
  currency("CVE", "Cape Verdean Escudo", "$", "🇨🇻", "Cape Verde", "Africa"),
  currency("CZK", "Czech Koruna", "Kč", "🇨🇿", "Czechia", "Europe"),
  currency("DJF", "Djiboutian Franc", "Fdj", "🇩🇯", "Djibouti", "Africa"),
  currency("DKK", "Danish Krone", "kr", "🇩🇰", "Denmark", "Europe"),
  currency("DOP", "Dominican Peso", "RD$", "🇩🇴", "Dominican Republic", "Caribbean"),
  currency("DZD", "Algerian Dinar", "دج", "🇩🇿", "Algeria", "Africa"),
  currency("EGP", "Egyptian Pound", "£", "🇪🇬", "Egypt", "Africa"),
  currency("ERN", "Eritrean Nakfa", "Nfk", "🇪🇷", "Eritrea", "Africa"),
  currency("ETB", "Ethiopian Birr", "Br", "🇪🇹", "Ethiopia", "Africa"),
  currency("FJD", "Fijian Dollar", "FJ$", "🇫🇯", "Fiji", "Oceania"),
  currency("FKP", "Falkland Islands Pound", "£", "🇫🇰", "Falkland Islands", "Atlantic"),
  currency("FOK", "Faroese Krona", "kr", "🇫🇴", "Faroe Islands", "Europe"),
  currency("GEL", "Georgian Lari", "₾", "🇬🇪", "Georgia", "Asia"),
  currency("GGP", "Guernsey Pound", "£", "🇬🇬", "Guernsey", "Europe"),
  currency("GHS", "Ghanaian Cedi", "₵", "🇬🇭", "Ghana", "Africa"),
  currency("GIP", "Gibraltar Pound", "£", "🇬🇮", "Gibraltar", "Europe"),
  currency("GMD", "Gambian Dalasi", "D", "🇬🇲", "Gambia", "Africa"),
  currency("GNF", "Guinean Franc", "FG", "🇬🇳", "Guinea", "Africa"),
  currency("GTQ", "Guatemalan Quetzal", "Q", "🇬🇹", "Guatemala", "Central America"),
  currency("GYD", "Guyanese Dollar", "$", "🇬🇾", "Guyana", "South America"),
  currency("HKD", "Hong Kong Dollar", "HK$", "🇭🇰", "Hong Kong", "Asia"),
  currency("HNL", "Honduran Lempira", "L", "🇭🇳", "Honduras", "Central America"),
  currency("HRK", "Croatian Kuna", "kn", "🇭🇷", "Croatia", "Europe"),
  currency("HTG", "Haitian Gourde", "G", "🇭🇹", "Haiti", "Caribbean"),
  currency("HUF", "Hungarian Forint", "Ft", "🇭🇺", "Hungary", "Europe"),
  currency("IDR", "Indonesian Rupiah", "Rp", "🇮🇩", "Indonesia", "Asia"),
  currency("ILS", "Israeli New Shekel", "₪", "🇮🇱", "Israel", "Middle East"),
  currency("IMP", "Isle of Man Pound", "£", "🇮🇲", "Isle of Man", "Europe"),
  currency("INR", "Indian Rupee", "₹", "🇮🇳", "India", "Asia"),
  currency("IQD", "Iraqi Dinar", "ع.د", "🇮🇶", "Iraq", "Middle East"),
  currency("IRR", "Iranian Rial", "﷼", "🇮🇷", "Iran", "Middle East"),
  currency("ISK", "Icelandic Krona", "kr", "🇮🇸", "Iceland", "Europe"),
  currency("JEP", "Jersey Pound", "£", "🇯🇪", "Jersey", "Europe"),
  currency("JMD", "Jamaican Dollar", "J$", "🇯🇲", "Jamaica", "Caribbean"),
  currency("JOD", "Jordanian Dinar", "د.ا", "🇯🇴", "Jordan", "Middle East"),
  currency("KES", "Kenyan Shilling", "KSh", "🇰🇪", "Kenya", "Africa"),
  currency("KGS", "Kyrgyzstani Som", "с", "🇰🇬", "Kyrgyzstan", "Asia"),
  currency("KHR", "Cambodian Riel", "៛", "🇰🇭", "Cambodia", "Asia"),
  currency("KID", "Kiribati Dollar", "$", "🇰🇮", "Kiribati", "Oceania"),
  currency("KMF", "Comorian Franc", "CF", "🇰🇲", "Comoros", "Africa"),
  currency("KRW", "South Korean Won", "₩", "🇰🇷", "South Korea", "Asia"),
  currency("KWD", "Kuwaiti Dinar", "د.ك", "🇰🇼", "Kuwait", "Middle East"),
  currency("KYD", "Cayman Islands Dollar", "$", "🇰🇾", "Cayman Islands", "Caribbean"),
  currency("KZT", "Kazakhstani Tenge", "₸", "🇰🇿", "Kazakhstan", "Asia"),
  currency("LAK", "Lao Kip", "₭", "🇱🇦", "Laos", "Asia"),
  currency("LBP", "Lebanese Pound", "ل.ل", "🇱🇧", "Lebanon", "Middle East"),
  currency("LKR", "Sri Lankan Rupee", "Rs", "🇱🇰", "Sri Lanka", "Asia"),
  currency("LRD", "Liberian Dollar", "$", "🇱🇷", "Liberia", "Africa"),
  currency("LSL", "Lesotho Loti", "L", "🇱🇸", "Lesotho", "Africa"),
  currency("LYD", "Libyan Dinar", "ل.د", "🇱🇾", "Libya", "Africa"),
  currency("MAD", "Moroccan Dirham", "د.م.", "🇲🇦", "Morocco", "Africa"),
  currency("MDL", "Moldovan Leu", "L", "🇲🇩", "Moldova", "Europe"),
  currency("MGA", "Malagasy Ariary", "Ar", "🇲🇬", "Madagascar", "Africa"),
  currency("MKD", "Macedonian Denar", "ден", "🇲🇰", "North Macedonia", "Europe"),
  currency("MMK", "Myanmar Kyat", "Ks", "🇲🇲", "Myanmar", "Asia"),
  currency("MNT", "Mongolian Tugrik", "₮", "🇲🇳", "Mongolia", "Asia"),
  currency("MOP", "Macanese Pataca", "MOP$", "🇲🇴", "Macau", "Asia"),
  currency("MRU", "Mauritanian Ouguiya", "UM", "🇲🇷", "Mauritania", "Africa"),
  currency("MUR", "Mauritian Rupee", "₨", "🇲🇺", "Mauritius", "Africa"),
  currency("MVR", "Maldivian Rufiyaa", "Rf", "🇲🇻", "Maldives", "Asia"),
  currency("MWK", "Malawian Kwacha", "MK", "🇲🇼", "Malawi", "Africa"),
  currency("MYR", "Malaysian Ringgit", "RM", "🇲🇾", "Malaysia", "Asia"),
  currency("MZN", "Mozambican Metical", "MT", "🇲🇿", "Mozambique", "Africa"),
  currency("NAD", "Namibian Dollar", "N$", "🇳🇦", "Namibia", "Africa"),
  currency("NGN", "Nigerian Naira", "₦", "🇳🇬", "Nigeria", "Africa"),
  currency("NIO", "Nicaraguan Cordoba", "C$", "🇳🇮", "Nicaragua", "Central America"),
  currency("NOK", "Norwegian Krone", "kr", "🇳🇴", "Norway", "Europe"),
  currency("NPR", "Nepalese Rupee", "₨", "🇳🇵", "Nepal", "Asia"),
  currency("OMR", "Omani Rial", "ر.ع.", "🇴🇲", "Oman", "Middle East"),
  currency("PAB", "Panamanian Balboa", "B/.", "🇵🇦", "Panama", "Central America"),
  currency("PEN", "Peruvian Sol", "S/", "🇵🇪", "Peru", "South America"),
  currency("PGK", "Papua New Guinean Kina", "K", "🇵🇬", "Papua New Guinea", "Oceania"),
  currency("PHP", "Philippine Peso", "₱", "🇵🇭", "Philippines", "Asia"),
  currency("PKR", "Pakistani Rupee", "₨", "🇵🇰", "Pakistan", "Asia"),
  currency("PLN", "Polish Zloty", "zł", "🇵🇱", "Poland", "Europe"),
  currency("PYG", "Paraguayan Guarani", "₲", "🇵🇾", "Paraguay", "South America"),
  currency("QAR", "Qatari Riyal", "ر.ق", "🇶🇦", "Qatar", "Middle East"),
  currency("RON", "Romanian Leu", "lei", "🇷🇴", "Romania", "Europe"),
  currency("RSD", "Serbian Dinar", "дин", "🇷🇸", "Serbia", "Europe"),
  currency("RUB", "Russian Ruble", "₽", "🇷🇺", "Russia", "Europe"),
  currency("RWF", "Rwandan Franc", "FRw", "🇷🇼", "Rwanda", "Africa"),
  currency("SAR", "Saudi Riyal", "﷼", "🇸🇦", "Saudi Arabia", "Middle East"),
  currency("SBD", "Solomon Islands Dollar", "SI$", "🇸🇧", "Solomon Islands", "Oceania"),
  currency("SCR", "Seychellois Rupee", "₨", "🇸🇨", "Seychelles", "Africa"),
  currency("SDG", "Sudanese Pound", "ج.س.", "🇸🇩", "Sudan", "Africa"),
  currency("SEK", "Swedish Krona", "kr", "🇸🇪", "Sweden", "Europe"),
  currency("SHP", "Saint Helena Pound", "£", "🇸🇭", "Saint Helena", "Atlantic"),
  currency("SLE", "Sierra Leonean Leone", "Le", "🇸🇱", "Sierra Leone", "Africa"),
  currency("SLL", "Sierra Leonean Leone", "Le", "🇸🇱", "Sierra Leone", "Africa"),
  currency("SOS", "Somali Shilling", "Sh", "🇸🇴", "Somalia", "Africa"),
  currency("SRD", "Surinamese Dollar", "$", "🇸🇷", "Suriname", "South America"),
  currency("SSP", "South Sudanese Pound", "£", "🇸🇸", "South Sudan", "Africa"),
  currency("STN", "Sao Tome and Principe Dobra", "Db", "🇸🇹", "Sao Tome and Principe", "Africa"),
  currency("SYP", "Syrian Pound", "£", "🇸🇾", "Syria", "Middle East"),
  currency("SZL", "Eswatini Lilangeni", "L", "🇸🇿", "Eswatini", "Africa"),
  currency("THB", "Thai Baht", "฿", "🇹🇭", "Thailand", "Asia"),
  currency("TJS", "Tajikistani Somoni", "ЅМ", "🇹🇯", "Tajikistan", "Asia"),
  currency("TMT", "Turkmenistani Manat", "m", "🇹🇲", "Turkmenistan", "Asia"),
  currency("TND", "Tunisian Dinar", "د.ت", "🇹🇳", "Tunisia", "Africa"),
  currency("TOP", "Tongan Pa'anga", "T$", "🇹🇴", "Tonga", "Oceania"),
  currency("TRY", "Turkish Lira", "₺", "🇹🇷", "Turkey", "Europe"),
  currency("TTD", "Trinidad and Tobago Dollar", "TT$", "🇹🇹", "Trinidad and Tobago", "Caribbean"),
  currency("TVD", "Tuvaluan Dollar", "$", "🇹🇻", "Tuvalu", "Oceania"),
  currency("TWD", "New Taiwan Dollar", "NT$", "🇹🇼", "Taiwan", "Asia"),
  currency("TZS", "Tanzanian Shilling", "TSh", "🇹🇿", "Tanzania", "Africa"),
  currency("UAH", "Ukrainian Hryvnia", "₴", "🇺🇦", "Ukraine", "Europe"),
  currency("UGX", "Ugandan Shilling", "USh", "🇺🇬", "Uganda", "Africa"),
  currency("UYU", "Uruguayan Peso", "$U", "🇺🇾", "Uruguay", "South America"),
  currency("UZS", "Uzbekistani Som", "so'm", "🇺🇿", "Uzbekistan", "Asia"),
  currency("VES", "Venezuelan Bolivar", "Bs.", "🇻🇪", "Venezuela", "South America"),
  currency("VND", "Vietnamese Dong", "₫", "🇻🇳", "Vietnam", "Asia"),
  currency("VUV", "Vanuatu Vatu", "VT", "🇻🇺", "Vanuatu", "Oceania"),
  currency("WST", "Samoan Tala", "T", "🇼🇸", "Samoa", "Oceania"),
  currency("XAF", "Central African CFA Franc", "FCFA", "🌍", "CEMAC", "Africa"),
  currency("XCD", "East Caribbean Dollar", "EC$", "🌎", "Eastern Caribbean", "Caribbean"),
  currency("XCG", "Caribbean Guilder", "Cg", "🌎", "Caribbean", "Caribbean"),
  currency("XDR", "Special Drawing Rights", "SDR", "🌐", "IMF", "Global"),
  currency("XOF", "West African CFA Franc", "CFA", "🌍", "UEMOA", "Africa"),
  currency("XPF", "CFP Franc", "₣", "🇵🇫", "French Pacific", "Oceania"),
  currency("YER", "Yemeni Rial", "﷼", "🇾🇪", "Yemen", "Middle East"),
  currency("ZAR", "South African Rand", "R", "🇿🇦", "South Africa", "Africa"),
  currency("ZMW", "Zambian Kwacha", "ZK", "🇿🇲", "Zambia", "Africa"),
  currency("ZWL", "Zimbabwean Dollar", "Z$", "🇿🇼", "Zimbabwe", "Africa"),
  currency("BTC", "Bitcoin", "₿", "₿", "Bitcoin", "Crypto", true),
  currency("ETH", "Ethereum", "Ξ", "Ξ", "Ethereum", "Crypto", true),
  currency("USDT", "Tether", "₮", "₮", "Tether", "Crypto", true),
  currency("BNB", "BNB", "◆", "◆", "BNB", "Crypto", true),
  currency("XRP", "XRP", "✕", "✕", "XRP", "Crypto", true),
  currency("USDC", "USD Coin", "$", "$", "USD Coin", "Crypto", true),
  currency("SOL", "Solana", "◎", "◎", "Solana", "Crypto", true),
  currency("TRX", "TRON", "◆", "◆", "TRON", "Crypto"),
  currency("DOGE", "Dogecoin", "Ð", "Ð", "Dogecoin", "Crypto"),
  currency("ADA", "Cardano", "₳", "₳", "Cardano", "Crypto"),
  currency("HYPE", "Hyperliquid", "H", "H", "Hyperliquid", "Crypto"),
  currency("ZEC", "Zcash", "ⓩ", "ⓩ", "Zcash", "Crypto"),
  currency("BCH", "Bitcoin Cash", "₿", "₿", "Bitcoin Cash", "Crypto"),
  currency("LINK", "Chainlink", "◆", "◆", "Chainlink", "Crypto"),
  currency("XMR", "Monero", "ɱ", "ɱ", "Monero", "Crypto"),
  currency("TON", "Toncoin", "◆", "◆", "Toncoin", "Crypto"),
  currency("XLM", "Stellar", "*", "*", "Stellar", "Crypto"),
  currency("SUI", "Sui", "◆", "◆", "Sui", "Crypto"),
  currency("LTC", "Litecoin", "Ł", "Ł", "Litecoin", "Crypto"),
  currency("DAI", "Dai", "◈", "◈", "Dai", "Crypto"),
  currency("AVAX", "Avalanche", "▲", "▲", "Avalanche", "Crypto"),
  currency("HBAR", "Hedera", "ℏ", "ℏ", "Hedera", "Crypto"),
  currency("SHIB", "Shiba Inu", "◆", "◆", "Shiba Inu", "Crypto"),
  currency("CRO", "Cronos", "◆", "◆", "Cronos", "Crypto"),
  currency("TAO", "Bittensor", "τ", "τ", "Bittensor", "Crypto"),
  currency("XAUT", "Tether Gold", "Au", "Au", "Tether Gold", "Crypto"),
  currency("DOT", "Polkadot", "●", "●", "Polkadot", "Crypto"),
  currency("UNI", "Uniswap", "U", "U", "Uniswap", "Crypto"),
  currency("AAVE", "Aave", "A", "A", "Aave", "Crypto"),
  currency("NEAR", "NEAR Protocol", "N", "N", "NEAR Protocol", "Crypto"),
  currency("ETC", "Ethereum Classic", "Ξ", "Ξ", "Ethereum Classic", "Crypto"),
  currency("OKB", "OKB", "O", "O", "OKB", "Crypto"),
  currency("ICP", "Internet Computer", "∞", "∞", "Internet Computer", "Crypto"),
  currency("KAS", "Kaspa", "K", "K", "Kaspa", "Crypto"),
  currency("POL", "Polygon Ecosystem Token", "◇", "◇", "Polygon", "Crypto"),
  currency("ATOM", "Cosmos", "⚛", "⚛", "Cosmos", "Crypto"),
  currency("FIL", "Filecoin", "⨎", "⨎", "Filecoin", "Crypto"),
  currency("ARB", "Arbitrum", "A", "A", "Arbitrum", "Crypto"),
  currency("OP", "Optimism", "O", "O", "Optimism", "Crypto"),
  currency("INJ", "Injective", "I", "I", "Injective", "Crypto"),
  currency("RENDER", "Render", "R", "R", "Render", "Crypto"),
  currency("VET", "VeChain", "V", "V", "VeChain", "Crypto"),
  currency("ALGO", "Algorand", "A", "A", "Algorand", "Crypto"),
  currency("SEI", "Sei", "S", "S", "Sei", "Crypto"),
  currency("FET", "Artificial Superintelligence Alliance", "F", "F", "ASI Alliance", "Crypto"),
  currency("JUP", "Jupiter", "J", "J", "Jupiter", "Crypto"),
  currency("BONK", "Bonk", "B", "B", "Bonk", "Crypto"),
  currency("WIF", "dogwifhat", "W", "W", "dogwifhat", "Crypto"),
  currency("PEPE", "Pepe", "P", "P", "Pepe", "Crypto"),
  currency("PYUSD", "PayPal USD", "$", "$", "PayPal USD", "Crypto"),
];

const stablecoinSymbols = new Set(["USDT", "USDC", "DAI", "PYUSD", "USDS", "BUSD"]);

const providerCatalogItems: ProviderCatalogItem[] = [
  providerOption("wise", "Wise", "Transfer provider", "Live quote ready", ["global", "oceania", "latam", "europe", "asia"], ["AUD", "USD", "EUR", "GBP", "ARS", "BRL", "MXN", "COP", "JPY"], true, 100, "Official quote API path; first candidate for live pricing."),
  providerOption("revolut", "Revolut", "Transfer provider", "Partner API required", ["global", "oceania", "europe", "asia"], ["AUD", "USD", "EUR", "GBP", "JPY", "SGD"], true, 92, "Strong travel and multi-currency account route."),
  providerOption("moneygram", "MoneyGram", "Transfer provider", "Live quote ready", ["global", "latam", "asia", "africa"], ["USD", "AUD", "ARS", "BRL", "MXN", "COP", "PEN", "CLP"], true, 88, "Quote API supports cash, bank, wallet and card receive options."),
  providerOption("western_union", "Western Union", "Transfer provider", "Partner API required", ["global", "latam", "africa", "asia"], ["USD", "AUD", "ARS", "BRL", "MXN", "COP", "PEN", "CLP"], true, 82, "Cash pickup and bank fallback for broad corridors."),
  providerOption("remitly", "Remitly", "Transfer provider", "Partner API required", ["global", "latam", "asia", "africa"], ["USD", "AUD", "MXN", "COP", "PEN", "BRL"], true, 80, "Popular remittance option where official access requires partnership."),
  providerOption("paypal_xoom", "PayPal / Xoom", "Transfer provider", "Partner API required", ["global", "latam", "north_america"], ["USD", "AUD", "MXN", "ARS", "BRL", "COP", "PEN"], true, 74, "Useful where PayPal identity or Xoom corridors matter."),
  providerOption("remessa_online", "Remessa Online", "Transfer provider", "Partner API required", ["latam"], ["BRL", "USD", "EUR", "GBP"], true, 86, "Brazil-focused FX and international transfer provider."),
  providerOption("global66", "Global66", "Transfer provider", "Estimated", ["latam"], ["CLP", "ARS", "COP", "PEN", "MXN", "USD", "EUR"], true, 78, "LatAm app for wallet, card and international transfers."),
  providerOption("dolarapp", "DolarApp", "Digital dollar", "Estimated", ["latam"], ["ARS", "MXN", "COP", "BRL", "USD"], true, 70, "Digital-dollar account route for LatAm users."),
  providerOption("airtm", "Airtm", "Digital dollar", "Estimated", ["latam", "emerging"], ["ARS", "VES", "COP", "PEN", "USD"], true, 62, "Useful in high-friction emerging-market corridors."),
  providerOption("card_payment", "Card payment", "Local rail", "Estimated", ["global", "oceania", "latam", "europe", "asia"], ["AUD", "USD", "EUR", "ARS", "BRL", "MXN"], true, 58, "Emergency card route with markup visibility."),
  providerOption("atm_cash", "ATM cash", "Local rail", "Estimated", ["global", "oceania", "latam", "europe", "asia"], ["AUD", "USD", "EUR", "ARS", "BRL", "MXN"], true, 52, "Cash-access route with fee and spread estimates."),
  providerOption("bank_transfer", "Bank transfer", "Local rail", "Estimated", ["global", "oceania", "latam", "europe", "asia"], ["AUD", "USD", "EUR", "ARS", "BRL", "MXN"], true, 48, "Bank fallback when fintech providers are unavailable."),
  providerOption("airport_exchange", "Airport exchange", "Local rail", "Estimated", ["global", "oceania", "latam", "europe", "asia"], ["AUD", "USD", "EUR", "ARS", "BRL", "MXN"], true, 20, "Last-resort cash route; kept visible for avoidance decisions."),
  providerOption("paypal", "PayPal", "Wallet / payout", "Wallet only", ["global", "north_america", "latam", "oceania"], ["USD", "AUD", "MXN", "BRL", "ARS"], false, 45, "Wallet and payout method, not a direct FX quote source."),
  providerOption("venmo", "Venmo", "Wallet / payout", "Wallet only", ["north_america"], ["USD"], false, 38, "US wallet/payout method through PayPal rails."),
  providerOption("paypay", "PayPay", "Wallet / payout", "Wallet only", ["asia"], ["JPY"], false, 36, "Japan wallet/payment rail."),
  providerOption("mercado_pago", "Mercado Pago", "Wallet / payout", "Wallet only", ["latam"], ["ARS", "BRL", "MXN", "CLP", "COP", "PEN", "UYU"], false, 44, "LatAm wallet and local payment rail."),
  providerOption("pix", "Pix", "Local rail", "Wallet only", ["latam"], ["BRL"], false, 42, "Brazil instant payment rail."),
  providerOption("picpay", "PicPay", "Wallet / payout", "Wallet only", ["latam"], ["BRL"], false, 35, "Brazil wallet/Pix acceptance option."),
  providerOption("nequi", "Nequi", "Wallet / payout", "Wallet only", ["latam"], ["COP"], false, 34, "Colombia wallet and business payment API surface."),
  providerOption("yape", "Yape", "Wallet / payout", "Wallet only", ["latam"], ["PEN"], false, 32, "Peru wallet and local receive method."),
  providerOption("uala", "Uala", "Wallet / payout", "Wallet only", ["latam"], ["ARS", "MXN", "COP"], false, 31, "LatAm wallet/card route."),
];

const providerQuoteCacheVersion = "v6";

const wiseComparisonAliases = new Map<string, string>([
  ["wise", "wise"],
  ["revolut", "revolut"],
  ["moneygram", "moneygram"],
  ["western-union", "western_union"],
  ["western_union", "western_union"],
  ["remitly", "remitly"],
  ["paypal", "paypal_xoom"],
  ["xoom", "paypal_xoom"],
]);

const providerComparisonAliases = new Map<string, string>(
  Array.from(wiseComparisonAliases.entries()).map(([alias, id]) => [id, alias]),
);

const quoteTemplates = new Map<string, {
  fixedFee: number;
  feePercent: number;
  markupPercent: number;
  deliverySpeed: string;
  paymentMethod: string;
  riskLabel: string;
  bestFor: string;
  source: string;
  sourceUrl: string;
}>([
  ["wise", { fixedFee: 0.35, feePercent: 0.45, markupPercent: 0.05, deliverySpeed: "Same day", paymentMethod: "Debit/bank", riskLabel: "Low", bestFor: "Low-cost transfer", source: "FX Always estimate after Wise quote API fallback", sourceUrl: "https://docs.wise.com/api-reference/quote" }],
  ["revolut", { fixedFee: 0, feePercent: 0.8, markupPercent: 0.15, deliverySpeed: "Minutes", paymentMethod: "Card balance", riskLabel: "Low", bestFor: "Travel card spend", source: "FX Always estimate until Revolut Business API is configured", sourceUrl: "https://developer.revolut.com/docs/business/get-rate" }],
  ["moneygram", { fixedFee: 1.5, feePercent: 0.7, markupPercent: 0.6, deliverySpeed: "Minutes", paymentMethod: "Cash pickup", riskLabel: "Medium", bestFor: "Cash pickup", source: "FX Always estimate until MoneyGram OAuth is configured", sourceUrl: "https://developer.moneygram.com/moneygram-developer/reference/instructpayouttransactionquote" }],
  ["western_union", { fixedFee: 2.5, feePercent: 0.95, markupPercent: 1.2, deliverySpeed: "Same day", paymentMethod: "Cash pickup", riskLabel: "Medium", bestFor: "Broad cash network", source: "FX Always estimate; partner quote access required", sourceUrl: "https://www.westernunion.com" }],
  ["remitly", { fixedFee: 1.99, feePercent: 0.6, markupPercent: 0.95, deliverySpeed: "Same day", paymentMethod: "Bank or wallet", riskLabel: "Medium", bestFor: "Family remittance", source: "FX Always estimate; partner quote access required", sourceUrl: "https://www.remitly.com" }],
  ["paypal_xoom", { fixedFee: 2.99, feePercent: 0.8, markupPercent: 1.7, deliverySpeed: "Minutes", paymentMethod: "Wallet/bank", riskLabel: "Medium", bestFor: "PayPal identity", source: "FX Always estimate; Xoom partner quote access required", sourceUrl: "https://www.xoom.com" }],
  ["remessa_online", { fixedFee: 0.9, feePercent: 0.55, markupPercent: 0.35, deliverySpeed: "Same day", paymentMethod: "Bank account", riskLabel: "Low", bestFor: "Brazil transfers", source: "FX Always estimate; partner quote access required", sourceUrl: "https://www.remessaonline.com.br" }],
  ["global66", { fixedFee: 0.75, feePercent: 0.65, markupPercent: 0.55, deliverySpeed: "Same day", paymentMethod: "Wallet/bank", riskLabel: "Low", bestFor: "LatAm account route", source: "FX Always estimate; public quote API not configured", sourceUrl: "https://global66.com" }],
  ["dolarapp", { fixedFee: 3, feePercent: 0.2, markupPercent: 0.3, deliverySpeed: "1-2 days", paymentMethod: "Digital dollar", riskLabel: "Medium", bestFor: "Digital dollar", source: "FX Always estimate; wallet quote API not configured", sourceUrl: "https://www.dolarapp.com" }],
  ["airtm", { fixedFee: 1, feePercent: 1.1, markupPercent: 1.8, deliverySpeed: "Same day", paymentMethod: "Digital wallet", riskLabel: "High", bestFor: "Emerging markets", source: "FX Always estimate; wallet quote API not configured", sourceUrl: "https://www.airtm.com" }],
  ["card_payment", { fixedFee: 0, feePercent: 0.3, markupPercent: 2.7, deliverySpeed: "Instant", paymentMethod: "Card terminal", riskLabel: "Medium", bestFor: "Emergency card payment", source: "FX Always local rail model", sourceUrl: "" }],
  ["atm_cash", { fixedFee: 4, feePercent: 1, markupPercent: 3, deliverySpeed: "Instant", paymentMethod: "Cash withdrawal", riskLabel: "High", bestFor: "Cash access", source: "FX Always local rail model", sourceUrl: "" }],
  ["bank_transfer", { fixedFee: 5, feePercent: 0.8, markupPercent: 3.2, deliverySpeed: "1-2 days", paymentMethod: "Bank account", riskLabel: "Medium", bestFor: "Bank fallback", source: "FX Always local rail model", sourceUrl: "" }],
  ["airport_exchange", { fixedFee: 0, feePercent: 0, markupPercent: 8.5, deliverySpeed: "Instant", paymentMethod: "Airport cash", riskLabel: "Very high", bestFor: "Last resort", source: "FX Always local rail model", sourceUrl: "" }],
]);

export const latestRates = onRequest({ region, cors: true }, async (request, response) => {
  try {
    const base = normalizeCurrency(request.query.base, "USD");
    const payload = await getLatestRates(base);
    response.status(200).json(payload);
  } catch (error) {
    response.status(500).json({ message: errorMessage(error) });
  }
});

export const providerCatalog = onRequest({ region, cors: true }, async (request, response) => {
  try {
    const baseCurrency = normalizeCurrency(request.query.base, "USD");
    const payload = getProviderCatalog(baseCurrency);
    response.status(200).json(payload);
  } catch (error) {
    response.status(500).json({ message: errorMessage(error) });
  }
});

export const providerQuotes = onRequest(
  { region, cors: true },
  async (request, response) => {
    try {
      const base = normalizeCurrency(request.query.base, "USD");
      const target = normalizeCurrency(request.query.target, "EUR");
      const amount = normalizeAmount(request.query.amount, 100);
      const plan = normalizePlan(request.query.plan);
      const providers = normalizeProviderList(request.query.providers);
      const payload = await getProviderQuotes(base, target, amount, providers, plan);
      response.status(200).json(payload);
    } catch (error) {
      response.status(500).json({ message: errorMessage(error) });
    }
  },
);

export const supportedCurrencies = onRequest({ region, cors: true }, async (_request, response) => {
  try {
    const payload = await getSupportedCurrencies();
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

export const cryptoMarkets = onRequest({ region, cors: true }, async (request, response) => {
  try {
    const base = normalizeCurrency(request.query.base, "USD");
    const limit = normalizeLimit(request.query.limit, 200, 5, 200);
    const payload = await getCryptoMarkets(base, limit);
    response.status(200).json(payload);
  } catch (error) {
    response.status(500).json({ message: errorMessage(error) });
  }
});

export const newsFeed = onRequest({ region, cors: true, secrets: [marketauxApiKey] }, async (request, response) => {
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
    secrets: [marketauxApiKey],
  },
  async () => {
    await Promise.all([
      getNewsFeed("en", "US", ["USD", "EUR", "JPY", "GBP", "BTC"], true),
      getNewsFeed("es", "AR", ["USD", "EUR", "BRL", "BTC"], true),
      getNewsFeed("pt", "BR", ["USD", "BRL", "EUR", "BTC"], true),
    ]);
  },
);

export const refreshCryptoCache = onSchedule(
  {
    region,
    schedule: "every 10 minutes",
    timeZone: "Etc/UTC",
  },
  async () => {
    await getCryptoMarkets("USD", 200, true);
  },
);

export const refreshProviderQuoteCache = onSchedule(
  {
    region,
    schedule: "every 15 minutes",
    timeZone: "Etc/UTC",
  },
  async () => {
    await Promise.all([
      getProviderQuotes("AUD", "ARS", 500, ["wise", "revolut", "moneygram", "global66"], "pro", true),
      getProviderQuotes("USD", "MXN", 500, ["wise", "moneygram", "western_union", "remitly"], "pro", true),
      getProviderQuotes("EUR", "USD", 500, ["wise", "revolut", "bank_transfer"], "pro", true),
    ]);
  },
);

export const evaluateServerAlerts = onSchedule(
  {
    region,
    schedule: "every 15 minutes",
    timeZone: "Etc/UTC",
  },
  async () => {
    await evaluateBackedUpAlerts();
  },
);

async function getLatestRates(base: string, forceRefresh = false): Promise<LatestRatesResponse> {
  const ref = db.collection("fx_latest").doc(base);
  const cached = await ref.get();
  const cachedData = cached.data() as (LatestRatesResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  if (exchangeRateApiKey) {
    const exchangeRatePayload = await getExchangeRateApiLatest(base);
    await ref.set({
      ...exchangeRatePayload,
      expiresAt: Timestamp.fromMillis(Date.now() + 55 * 60 * 1000),
    });
    return exchangeRatePayload;
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

async function getExchangeRateApiLatest(base: string): Promise<LatestRatesResponse> {
  const url = new URL(`https://v6.exchangerate-api.com/v6/${exchangeRateApiKey}/latest/${base}`);
  const upstream = await fetchJson<ExchangeRateApiLatestResponse>(url);
  if (upstream.result && upstream.result !== "success") {
    throw new Error(`ExchangeRate-API error: ${upstream["error-type"] ?? upstream.result}`);
  }
  const rates = Object.entries(upstream.conversion_rates ?? {})
    .filter(([code, value]) => code !== base && typeof value === "number" && Number.isFinite(value))
    .map(([code, value]) => ({ code, value }))
    .sort((left, right) => left.code.localeCompare(right.code));

  return {
    base: upstream.base_code ?? base,
    date: formatDate(new Date()),
    rates,
    provider: "ExchangeRate-API",
    refreshedAt: new Date().toISOString(),
  };
}

async function getSupportedCurrencies(): Promise<SupportedCurrenciesResponse> {
  const ref = db.collection("fx_meta").doc("supported_currencies");
  const cached = await ref.get();
  const cachedData = cached.data() as (SupportedCurrenciesResponse & { expiresAt?: Timestamp }) | undefined;

  if (cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  const payload: SupportedCurrenciesResponse = {
    provider: exchangeRateApiKey ? "ExchangeRate-API catalog" : "FX Always catalog",
    refreshedAt: new Date().toISOString(),
    currencies: currencyCatalog,
  };

  await ref.set({
    ...payload,
    expiresAt: Timestamp.fromMillis(Date.now() + 24 * 60 * 60 * 1000),
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

async function getCryptoMarkets(base: string, limit: number, forceRefresh = false): Promise<CryptoMarketsResponse> {
  const docId = `${base}_${limit}`;
  const ref = db.collection("crypto_markets").doc(docId);
  const cached = await ref.get();
  const cachedData = cached.data() as (CryptoMarketsResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  try {
    const payload = await fetchCoinPaprikaMarkets(base, limit);
    await ref.set({
      ...payload,
      expiresAt: Timestamp.fromMillis(Date.now() + 10 * 60 * 1000),
    });
    return payload;
  } catch (error) {
    if (cachedData) {
      return stripExpiry(cachedData);
    }
    throw error;
  }
}

async function fetchCoinPaprikaMarkets(base: string, limit: number): Promise<CryptoMarketsResponse> {
  const url = new URL(`${coinPaprikaBaseUrl}/tickers`);
  url.searchParams.set("quotes", "USD");
  const upstream = await fetchJson<CoinPaprikaTicker[]>(url);
  const usdToBase = await getUsdToBaseRate(base);
  const catalogByCode = new Map(currencyCatalog.filter((item) => item.region === "Crypto").map((item) => [item.code, item]));
  const seen = new Set<string>();
  const assets: CryptoMarketAsset[] = [];
  const rankedTickers = upstream
    .slice()
    .sort((left, right) => (left.rank ?? Number.MAX_SAFE_INTEGER) - (right.rank ?? Number.MAX_SAFE_INTEGER));

  for (const ticker of rankedTickers) {
    const code = ticker.symbol?.toUpperCase();
    const quote = ticker.quotes?.USD;
    const priceUsd = quote?.price;
    if (!code || seen.has(code) || typeof priceUsd !== "number" || !Number.isFinite(priceUsd) || priceUsd <= 0) {
      continue;
    }
    const catalog = catalogByCode.get(code);
    const priceBase = priceUsd * usdToBase;
    assets.push({
      code,
      name: catalog?.name ?? ticker.name ?? code,
      glyph: catalog?.flag ?? catalog?.symbol ?? "◆",
      kind: "Crypto",
      stable: stablecoinSymbols.has(code),
      rank: typeof ticker.rank === "number" ? ticker.rank : null,
      priceUsd,
      priceBase,
      value: 1 / priceBase,
      change24h: finiteOrZero(quote?.percent_change_24h),
      marketCapUsd: finiteOrNull(quote?.market_cap),
      volume24hUsd: finiteOrNull(quote?.volume_24h),
      sparkline: cryptoSparkline(priceBase, quote),
    });
    seen.add(code);
    if (assets.length >= limit) {
      break;
    }
  }

  return {
    base,
    provider: "CoinPaprika",
    refreshedAt: new Date().toISOString(),
    assets: assets.sort((left, right) => (left.rank ?? Number.MAX_SAFE_INTEGER) - (right.rank ?? Number.MAX_SAFE_INTEGER)),
  };
}

async function getUsdToBaseRate(base: string): Promise<number> {
  if (base === "USD") return 1;
  const latest = await getLatestRates("USD");
  const rate = latest.rates.find((item) => item.code === base)?.value;
  return typeof rate === "number" && Number.isFinite(rate) && rate > 0 ? rate : 1;
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
  const feedKey = `v2_${language}_${regionCode}_${currencies.join("-")}`;
  const ref = db.collection("fx_news_feeds").doc(feedKey);
  const cached = await ref.get();
  const cachedData = cached.data() as (NewsFeedResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  let payload: NewsFeedResponse;
  try {
    payload = marketauxApiKeyValue()
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
  url.searchParams.set("api_token", marketauxApiKeyValue());
  url.searchParams.set("language", language);
  url.searchParams.set("search", marketauxSearchQuery(currencies));
  url.searchParams.set("group_similar", "true");
  url.searchParams.set("limit", "3");

  const upstream = await fetchJson<MarketauxResponse>(url);
  const items = (upstream.data ?? []).slice(0, 3).map((article, index) => {
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

  if (items.length === 0) {
    throw new Error("Marketaux returned no FX stories");
  }

  return assembleNewsFeed(feedKey, language, regionCode, currencies, "Marketaux", items);
}

function marketauxApiKeyValue(): string {
  return marketauxApiKey.value() || process.env.MARKETAUX_API_KEY || "";
}

function marketauxSearchQuery(currencies: string[]): string {
  const currencyTerms = currencies
    .flatMap((code) => currencyNewsTerms(code).slice(0, 2))
    .slice(0, 10);
  return Array.from(new Set([
    ...currencyTerms,
    "\"foreign exchange\"",
    "\"central bank\"",
    "inflation",
    "rates",
  ])).join(" | ");
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

async function evaluateBackedUpAlerts(): Promise<void> {
  const backups = await db.collectionGroup("backups").get();
  const activeBackups = backups.docs
    .map((doc) => {
      const uid = doc.ref.parent.parent?.id;
      const snapshot = parseBackupSnapshot(doc.get("payloadJson"));
      return uid && snapshot?.alerts?.some((alert) => alert.enabled !== false) ? { doc, uid, snapshot } : null;
    })
    .filter((item): item is { doc: QueryDocumentSnapshot; uid: string; snapshot: UserBackupSnapshot } => Boolean(item));

  if (activeBackups.length === 0) return;

  const now = Date.now();
  const uniqueBases = Array.from(new Set(activeBackups.flatMap((item) => item.snapshot.alerts ?? []).filter(isActiveAlert).map((alert) => alert.base)));
  const ratesByBase = new Map<string, LatestRatesResponse>();
  await Promise.all(uniqueBases.map(async (base) => ratesByBase.set(base, await getLatestRates(base))));

  const dailyPairs = Array.from(new Set(
    activeBackups
      .flatMap((item) => item.snapshot.alerts ?? [])
      .filter((alert) => isActiveAlert(alert) && normalizeAlertKind(alert.kind) === "DailyChange")
      .map((alert) => `${alert.base}/${alert.quote}`),
  ));
  const dailyMovesByPair = new Map<string, number | null>();
  await Promise.all(dailyPairs.map(async (pair) => {
    const [base, quote] = pair.split("/");
    const history = await getHistoricalRates(base, quote, 2);
    dailyMovesByPair.set(pair, dailyChangePct(history.points));
  }));

  await Promise.all(activeBackups.map(async ({ doc, uid, snapshot }) => {
    const events: ServerAlertEvent[] = [];
    const nextAlerts = (snapshot.alerts ?? []).map((alert) => {
      const kind = normalizeAlertKind(alert.kind);
      const currentRate = ratesByBase.get(alert.base)?.rates.find((rate) => rate.code === alert.quote)?.value ?? null;
      const currentDailyChange = dailyMovesByPair.get(`${alert.base}/${alert.quote}`) ?? null;
      const observedValue = kind === "DailyChange" ? currentDailyChange : currentRate;
      if (!shouldTriggerServerAlert(alert, observedValue, now)) return alert;

      events.push({
        alertId: alert.id,
        uid,
        base: alert.base,
        quote: alert.quote,
        kind,
        direction: alert.direction,
        target: alert.target,
        observedValue: observedValue ?? 0,
        triggeredAtMillis: now,
        source: "server",
      });
      return { ...alert, kind, lastTriggeredAtMillis: now };
    });

    if (events.length === 0) return;

    const updatedSnapshot: UserBackupSnapshot = {
      ...snapshot,
      updatedAtMillis: now,
      alerts: nextAlerts,
    };
    const batch = db.batch();
    batch.set(doc.ref, {
      payloadJson: JSON.stringify(updatedSnapshot),
      schemaVersion: updatedSnapshot.schemaVersion ?? 1,
      updatedAtMillis: now,
      serverAlertEvaluatedAt: Timestamp.fromMillis(now),
    }, { merge: true });
    for (const event of events) {
      const eventId = `${event.alertId}_${event.triggeredAtMillis}`;
      batch.set(db.collection("users").doc(uid).collection("server_alert_events").doc(eventId), {
        ...event,
        triggeredAt: Timestamp.fromMillis(event.triggeredAtMillis),
      });
    }
    await batch.commit();
    await sendAlertPushes(uid, events, snapshot.settings?.language);
  }));
}

async function sendAlertPushes(uid: string, events: ServerAlertEvent[], language: string | undefined): Promise<void> {
  const tokensSnapshot = await db.collection("users").doc(uid).collection("push_tokens")
    .where("enabled", "==", true)
    .get();
  const tokenDocs = tokensSnapshot.docs
    .map((doc) => ({ doc, data: doc.data() as PushTokenDoc }))
    .filter((item) => typeof item.data.token === "string" && item.data.token.length > 0);
  if (tokenDocs.length === 0) return;

  for (const event of events) {
    const title = alertPushTitle(event, language);
    const body = alertPushBody(event, language);
    const message: MulticastMessage = {
      tokens: tokenDocs.map((item) => item.data.token as string),
      notification: {
        title,
        body,
      },
      data: {
        type: "price_alert",
        alertId: event.alertId,
        base: event.base,
        quote: event.quote,
        kind: event.kind,
        direction: event.direction,
        target: String(event.target),
        observedValue: String(event.observedValue),
        language: normalizeAlertLanguage(language),
        title,
        body,
      },
      android: {
        priority: "high",
        notification: {
          channelId: "price_alerts",
          clickAction: "android.intent.action.MAIN",
        },
      },
    };
    const result = await messaging.sendEachForMulticast(message);
    const cleanupBatch = db.batch();
    let cleanupWrites = 0;
    result.responses.forEach((response, index) => {
      const code = response.error?.code ?? "";
      if (code === "messaging/registration-token-not-registered" || code === "messaging/invalid-registration-token") {
        cleanupBatch.set(tokenDocs[index].doc.ref, {
          enabled: false,
          disabledAt: Timestamp.fromMillis(Date.now()),
          disabledReason: code,
        }, { merge: true });
        cleanupWrites += 1;
      }
    });
    if (cleanupWrites > 0) {
      await cleanupBatch.commit();
    }
  }
}

function alertPushTitle(event: ServerAlertEvent, language: string | undefined): string {
  const copy = alertNotificationCopy(language);
  return `${event.base}/${event.quote} ${copy.alertHit}`;
}

function alertPushBody(event: ServerAlertEvent, language: string | undefined): string {
  const copy = alertNotificationCopy(language);
  if (event.kind === "DailyChange") {
    const direction = event.direction === "Above" ? copy.up : copy.down;
    return `${direction} ${formatPercent(event.target)} · ${copy.twentyFourHour} ${formatSignedPercent(event.observedValue)}`;
  }
  const direction = event.direction === "Above" ? copy.above : copy.below;
  return `${direction} ${formatNumber(event.target)} · ${copy.now} ${formatNumber(event.observedValue)}`;
}

function alertNotificationCopy(language: string | undefined): AlertNotificationCopy {
  const copies: Record<string, AlertNotificationCopy> = {
    en: { alertHit: "alert hit", above: "Above", below: "Below", up: "Up", down: "Down", now: "now", twentyFourHour: "24h" },
    es: { alertHit: "alcanzó la alerta", above: "Por encima de", below: "Por debajo de", up: "Sube", down: "Baja", now: "ahora", twentyFourHour: "24 h" },
    pt: { alertHit: "atingiu o alerta", above: "Acima de", below: "Abaixo de", up: "Sobe", down: "Cai", now: "agora", twentyFourHour: "24 h" },
    zh: { alertHit: "已触发提醒", above: "高于", below: "低于", up: "上涨", down: "下跌", now: "当前", twentyFourHour: "24小时" },
    hi: { alertHit: "अलर्ट चालू हुआ", above: "ऊपर", below: "नीचे", up: "ऊपर", down: "नीचे", now: "अभी", twentyFourHour: "24घं" },
    fr: { alertHit: "a déclenché l'alerte", above: "Au-dessus de", below: "Sous", up: "Hausse", down: "Baisse", now: "maintenant", twentyFourHour: "24 h" },
    ar: { alertHit: "تم تشغيل التنبيه", above: "فوق", below: "تحت", up: "صعود", down: "هبوط", now: "الآن", twentyFourHour: "24س" },
    bn: { alertHit: "অ্যালার্ট চালু হয়েছে", above: "উপরে", below: "নিচে", up: "উপরে", down: "নিচে", now: "এখন", twentyFourHour: "২৪ঘ" },
    ru: { alertHit: "сработало", above: "Выше", below: "Ниже", up: "Рост", down: "Падение", now: "сейчас", twentyFourHour: "24 ч" },
    ur: { alertHit: "الرٹ چل گیا", above: "اوپر", below: "نیچے", up: "اوپر", down: "نیچے", now: "اب", twentyFourHour: "24گھنٹے" },
    id: { alertHit: "memicu peringatan", above: "Di atas", below: "Di bawah", up: "Naik", down: "Turun", now: "sekarang", twentyFourHour: "24 jam" },
    de: { alertHit: "Alarm ausgelöst", above: "Über", below: "Unter", up: "Steigt", down: "Fällt", now: "jetzt", twentyFourHour: "24 h" },
    ja: { alertHit: "アラート発火", above: "上回る", below: "下回る", up: "上昇", down: "下落", now: "現在", twentyFourHour: "24時間" },
  };
  return copies[normalizeAlertLanguage(language)] ?? copies.en;
}

function normalizeAlertLanguage(language: string | undefined): string {
  const normalized = (language ?? "en").toLowerCase().split(/[-_]/)[0];
  return normalized.length > 0 ? normalized : "en";
}

function formatNumber(value: number): string {
  if (!Number.isFinite(value)) return "--";
  return value.toLocaleString("en-US", { maximumFractionDigits: Math.abs(value) < 1 ? 6 : 4 });
}

function formatPercent(value: number): string {
  return `${formatNumber(value)}%`;
}

function formatSignedPercent(value: number): string {
  const sign = value >= 0 ? "+" : "";
  return `${sign}${formatNumber(value)}%`;
}

function parseBackupSnapshot(payloadJson: unknown): UserBackupSnapshot | null {
  if (typeof payloadJson !== "string" || payloadJson.trim().length === 0) return null;
  try {
    return JSON.parse(payloadJson) as UserBackupSnapshot;
  } catch {
    return null;
  }
}

function isActiveAlert(alert: PriceAlert): boolean {
  return alert.enabled !== false &&
    /^[A-Z]{3}$/.test(alert.base) &&
    /^[A-Z]{3,5}$/.test(alert.quote) &&
    Number.isFinite(alert.target);
}

function normalizeAlertKind(kind: PriceAlertKind | undefined): PriceAlertKind {
  return kind === "DailyChange" ? "DailyChange" : "Target";
}

function shouldTriggerServerAlert(alert: PriceAlert, observedValue: number | null, now: number): boolean {
  if (!isActiveAlert(alert) || observedValue == null || !Number.isFinite(observedValue)) return false;
  const kind = normalizeAlertKind(alert.kind);
  const crossed = kind === "DailyChange"
    ? alert.direction === "Above"
      ? observedValue >= alert.target
      : observedValue <= -alert.target
    : alert.direction === "Above"
      ? observedValue >= alert.target
      : observedValue <= alert.target;
  const lastTriggeredAt = alert.lastTriggeredAtMillis ?? null;
  const outsideCooldown = lastTriggeredAt == null || now - lastTriggeredAt >= SERVER_ALERT_COOLDOWN_MILLIS;
  return crossed && outsideCooldown;
}

function dailyChangePct(points: Array<{ date: string; value: number }>): number | null {
  const sorted = [...points].sort((left, right) => left.date.localeCompare(right.date));
  const previous = sorted.at(-2)?.value;
  const current = sorted.at(-1)?.value;
  if (!previous || current == null) return null;
  return ((current - previous) / previous) * 100;
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

async function fetchJsonRequest<T>(url: URL, init: RequestInit): Promise<T> {
  const response = await fetch(url, init);
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

function normalizeAmount(value: unknown, fallback: number): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = typeof raw === "string" ? Number.parseFloat(raw) : fallback;
  if (!Number.isFinite(parsed)) return fallback;
  return Math.min(Math.max(parsed, 0.01), 1_000_000);
}

function normalizePlan(value: unknown): "free" | "pro" {
  const raw = Array.isArray(value) ? value[0] : value;
  return raw === "pro" ? "pro" : "free";
}

function normalizeProviderList(value: unknown): string[] {
  const raw = Array.isArray(value) ? value.join(",") : value;
  if (typeof raw !== "string") return [];
  const validIds = new Set(providerCatalogItems.map((provider) => provider.id));
  return raw
    .split(",")
    .map((item) => item.trim().toLowerCase())
    .filter((item, index, items) => validIds.has(item) && items.indexOf(item) === index);
}

function normalizeDays(value: unknown): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = typeof raw === "string" ? Number.parseInt(raw, 10) : 365;
  if (!Number.isFinite(parsed)) return 365;
  return Math.min(Math.max(parsed, 7), 1825);
}

function normalizeLimit(value: unknown, fallback: number, min: number, max: number): number {
  const raw = Array.isArray(value) ? value[0] : value;
  const parsed = typeof raw === "string" ? Number.parseInt(raw, 10) : fallback;
  if (!Number.isFinite(parsed)) return fallback;
  return Math.min(Math.max(parsed, min), max);
}

function marketForCurrency(currencyCode: string): string {
  switch (currencyCode.toUpperCase()) {
    case "AUD":
    case "NZD":
      return "oceania";
    case "ARS":
    case "BRL":
    case "MXN":
    case "CLP":
    case "COP":
    case "PEN":
    case "UYU":
    case "VES":
    case "BOB":
    case "PYG":
      return "latam";
    case "USD":
    case "CAD":
      return "north_america";
    case "EUR":
    case "GBP":
    case "CHF":
    case "DKK":
    case "NOK":
    case "SEK":
    case "PLN":
      return "europe";
    case "JPY":
    case "CNY":
    case "SGD":
    case "HKD":
    case "INR":
    case "PHP":
    case "THB":
    case "IDR":
      return "asia";
    default:
      return "global";
  }
}

function getProviderCatalog(baseCurrency: string): ProviderCatalogResponse {
  const regionKey = marketForCurrency(baseCurrency);
  const primary = providerCatalogItems
    .filter((provider) => provider.markets.includes("global") || provider.markets.includes(regionKey) || provider.currencies.includes(baseCurrency))
    .sort(compareProviders);
  const primaryIds = new Set(primary.map((provider) => provider.id));
  const other = providerCatalogItems
    .filter((provider) => !primaryIds.has(provider.id))
    .sort(compareProviders);
  return {
    provider: "FX Always provider catalog",
    refreshedAt: new Date().toISOString(),
    region: regionKey,
    baseCurrency,
    primary,
    other,
  };
}

async function getProviderQuotes(
  base: string,
  target: string,
  amount: number,
  requestedProviders: string[],
  plan: "free" | "pro",
  forceRefresh = false,
): Promise<ProviderQuotesResponse> {
  const providerIds = selectedQuoteProviderIds(base, target, requestedProviders, plan);
  const amountBucket = Math.round(amount * 100) / 100;
  const docId = `${providerQuoteCacheVersion}_${base}_${target}_${amountBucket}_${plan}_${providerIds.join("-")}`;
  const ref = db.collection("provider_quotes").doc(docId);
  const cached = await ref.get();
  const cachedData = cached.data() as (ProviderQuotesResponse & { expiresAt?: Timestamp }) | undefined;

  if (!forceRefresh && cachedData?.expiresAt && cachedData.expiresAt.toMillis() > Date.now()) {
    return stripExpiry(cachedData);
  }

  const midMarketRate = await getMidMarketRate(base, target);
  const midMarketTarget = amount * midMarketRate;
  const refreshedAt = new Date().toISOString();
  const comparisonQuotes = await getWiseComparisonQuotes(base, target, amount, providerIds, midMarketRate, midMarketTarget, refreshedAt);
  const comparisonByProvider = new Map(comparisonQuotes.map((quote) => [quote.providerId, quote]));
  const quotes = await Promise.all(
    providerIds.map((providerId) => buildProviderQuote(providerId, base, target, amount, midMarketRate, midMarketTarget, refreshedAt, comparisonByProvider.get(providerId))),
  );
  const payload: ProviderQuotesResponse = {
    provider: "FX Always provider quotes",
    refreshedAt,
    base,
    target,
    amount,
    plan,
    midMarketRate,
    midMarketTarget,
    quotes: quotes.sort((left, right) => left.lossAmount - right.lossAmount || left.provider.localeCompare(right.provider)),
  };

  await ref.set({
    ...payload,
    expiresAt: Timestamp.fromMillis(Date.now() + 10 * 60 * 1000),
  });

  return payload;
}

async function buildProviderQuote(
  providerId: string,
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
  comparisonQuote?: ProviderQuote,
): Promise<ProviderQuote> {
  const provider = providerCatalogItems.find((item) => item.id === providerId);
  if (!provider || !provider.quoteCapable || !provider.currencies.includes(base) || !provider.currencies.includes(target)) {
    return unavailableQuote(providerId, base, target, amount, midMarketRate, midMarketTarget, refreshedAt, "Provider does not support this route in the current catalog.");
  }

  if (providerId === "wise") {
    const wiseQuote = await getWiseQuote(base, target, amount, midMarketRate, midMarketTarget, refreshedAt);
    if (wiseQuote) return wiseQuote;
  }
  if (comparisonQuote) return comparisonQuote;
  if (providerId === "revolut") {
    const revolutQuote = await getRevolutQuote(base, target, amount, midMarketRate, midMarketTarget, refreshedAt);
    if (revolutQuote) return revolutQuote;
  }
  if (providerId === "moneygram" && configuredEnv("MONEYGRAM_ACCESS_TOKEN") && configuredEnv("MONEYGRAM_PARTNER_ID")) {
    const moneyGramQuote = await getMoneyGramQuote(base, target, amount, midMarketRate, midMarketTarget, refreshedAt);
    if (moneyGramQuote) return moneyGramQuote;
  }

  return estimatedProviderQuote(provider, base, target, amount, midMarketRate, midMarketTarget, refreshedAt);
}

async function getWiseQuote(
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
): Promise<ProviderQuote | null> {
  const url = new URL(`${process.env.WISE_API_BASE_URL ?? "https://api.wise.com"}/v3/quotes`);
  const token = configuredEnv("WISE_API_TOKEN");
  try {
    const upstream = await fetchJsonRequest<WiseQuoteResponse>(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        sourceCurrency: base,
        targetCurrency: target,
        sourceAmount: amount,
      }),
    });
    const payment = upstream.paymentOptions
      ?.filter((option) => option.disabled !== true && option.targetAmount && option.targetAmount > 0)
      .sort((left, right) => finiteOrZero(left.price?.total?.value?.amount) - finiteOrZero(right.price?.total?.value?.amount))[0]
      ?? upstream.paymentOptions?.find((option) => option.targetAmount && option.targetAmount > 0)
      ?? upstream.paymentOptions?.[0];
    const receivedAmount = finitePositive(upstream.targetAmount) ?? finitePositive(payment?.targetAmount);
    const effectiveRate = finitePositive(upstream.rate) ?? (receivedAmount ? receivedAmount / amount : midMarketRate);
    if (!receivedAmount || !effectiveRate || !payment) return null;
    const feeAmount = finiteOrZero(payment?.price?.total?.value?.amount);
    return completeQuote({
      providerId: "wise",
      provider: "Wise",
      status: "live",
      source: token ? "Wise Platform quote API" : "Wise Platform unauthenticated quote",
      sourceUrl: "https://docs.wise.com/api-reference/quote",
      amount,
      base,
      target,
      receivedAmount,
      feeAmount,
      effectiveRate,
      midMarketRate,
      midMarketTarget,
      deliverySpeed: payment?.formattedEstimatedDelivery ?? payment?.estimatedDelivery ?? "Same day",
      paymentMethod: payment?.payIn?.replaceAll("_", " ").toLowerCase() ?? "Debit/bank",
      riskLabel: "Low",
      bestFor: "Low-cost transfer",
      quoteMode: "Live quote ready",
      refreshedAt,
      expiresAt: upstream.rateExpirationTime ?? upstream.expirationTime,
      message: token ? "Live provider quote from backend." : "Live provider sandbox quote; configure partner token before relying on production pricing.",
    });
  } catch {
    return null;
  }
}

async function getWiseComparisonQuotes(
  base: string,
  target: string,
  amount: number,
  providerIds: string[],
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
): Promise<ProviderQuote[]> {
  const aliases = providerIds
    .map((id) => providerComparisonAliases.get(id))
    .filter((alias): alias is string => Boolean(alias));
  if (aliases.length === 0) return [];

  const url = new URL(`${process.env.WISE_API_BASE_URL ?? "https://api.wise.com"}/v4/comparisons`);
  url.searchParams.set("sourceCurrency", base);
  url.searchParams.set("targetCurrency", target);
  url.searchParams.set("sendAmount", amount.toString());
  url.searchParams.set("providers", aliases.join(","));
  url.searchParams.set("excludePartners", "true");
  url.searchParams.set("includeWise", "true");

  try {
    const upstream = await fetchJson<WiseComparisonResponse>(url);
    return upstream.providers
      ?.map((provider) => wiseComparisonProviderQuote(provider, base, target, amount, midMarketRate, midMarketTarget, refreshedAt))
      .filter((quote): quote is ProviderQuote => Boolean(quote))
      ?? [];
  } catch {
    return [];
  }
}

function wiseComparisonProviderQuote(
  comparisonProvider: WiseComparisonProvider,
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
): ProviderQuote | null {
  const providerId = wiseComparisonAliases.get((comparisonProvider.alias ?? "").toLowerCase());
  if (!providerId) return null;
  const catalogProvider = providerCatalogItems.find((item) => item.id === providerId);
  if (!catalogProvider) return null;
  const quote = comparisonProvider.quotes
    ?.filter((item) => finitePositive(item.receivedAmount) && finitePositive(item.rate))
    .sort((left, right) => (right.receivedAmount ?? 0) - (left.receivedAmount ?? 0))[0];
  if (!quote?.receivedAmount || !quote.rate) return null;

  return completeQuote({
    providerId,
    provider: catalogProvider.label,
    status: "comparison",
    source: "Wise market comparison",
    sourceUrl: "https://docs.wise.com/api-reference/comparison/comparisongetv3",
    amount,
    base,
    target,
    receivedAmount: quote.receivedAmount,
    feeAmount: finiteOrZero(quote.fee),
    effectiveRate: amount > 0 ? quote.receivedAmount / amount : quote.rate,
    midMarketRate,
    midMarketTarget,
    deliverySpeed: comparisonDurationLabel(quote),
    paymentMethod: comparisonProvider.type === "bank" ? "Bank transfer" : "Provider transfer",
    riskLabel: "Medium",
    bestFor: catalogProvider.subtitle.includes("cash") ? "Cash pickup" : "Market comparison",
    quoteMode: catalogProvider.quoteMode,
    refreshedAt,
    expiresAt: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
    message: "Market comparison only; not a locked provider quote.",
  });
}

async function getRevolutQuote(
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
): Promise<ProviderQuote | null> {
  const token = configuredEnv("REVOLUT_API_TOKEN");
  if (!token) return null;
  const url = new URL(`${process.env.REVOLUT_API_BASE_URL ?? "https://b2b.revolut.com/api/1.0"}/rate`);
  url.searchParams.set("from", base);
  url.searchParams.set("to", target);
  url.searchParams.set("amount", amount.toString());
  try {
    const upstream = await fetchJsonRequest<RevolutRateResponse>(url, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const receivedAmount = finitePositive(upstream.to?.amount) ?? amount * finiteOrZero(upstream.rate);
    const effectiveRate = amount > 0 ? receivedAmount / amount : finiteOrZero(upstream.rate);
    if (!receivedAmount || !effectiveRate) return null;
    return completeQuote({
      providerId: "revolut",
      provider: "Revolut",
      status: "live",
      source: "Revolut Business exchange rate API",
      sourceUrl: "https://developer.revolut.com/docs/business/get-rate",
      amount,
      base,
      target,
      receivedAmount,
      feeAmount: finiteOrZero(upstream.fee?.amount),
      effectiveRate,
      midMarketRate,
      midMarketTarget,
      deliverySpeed: "Minutes",
      paymentMethod: "Card balance",
      riskLabel: "Low",
      bestFor: "Travel card spend",
      quoteMode: "Partner API required",
      refreshedAt,
      message: "Live provider quote from backend.",
    });
  } catch {
    return null;
  }
}

async function getMoneyGramQuote(
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
): Promise<ProviderQuote | null> {
  const token = configuredEnv("MONEYGRAM_ACCESS_TOKEN");
  const partnerId = configuredEnv("MONEYGRAM_PARTNER_ID");
  if (!token || !partnerId) return null;
  const url = new URL(`${process.env.MONEYGRAM_API_BASE_URL ?? "https://sandboxapi.moneygram.com"}/transfer/v1/transactions/quote`);
  try {
    const upstream = await fetchJsonRequest<Record<string, unknown>>(url, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
        "X-MG-ClientRequestId": clientRequestId(),
      },
      body: JSON.stringify({
        targetAudience: "CUSTOMER_FACING",
        agentPartnerId: partnerId,
        destinationCountryCode: countryForCurrency(target),
        sendAmount: {
          value: amount,
          currencyCode: base,
        },
        receiveCurrencyCode: target,
        sendAmountIncludingFee: true,
      }),
    });
    const flattened = JSON.stringify(upstream);
    const receivedAmount = firstNumber(flattened, /"receiveAmount"\s*:\s*\{[^}]*"value"\s*:\s*([0-9.]+)/)
      ?? firstNumber(flattened, /"receiveAmount"\s*:\s*([0-9.]+)/);
    const feeAmount = firstNumber(flattened, /"feeAmount"\s*:\s*\{[^}]*"value"\s*:\s*([0-9.]+)/) ?? 0;
    const exchangeRate = firstNumber(flattened, /"exchangeRate"\s*:\s*([0-9.]+)/) ?? (receivedAmount ? receivedAmount / amount : midMarketRate);
    if (!receivedAmount || !exchangeRate) return null;
    return completeQuote({
      providerId: "moneygram",
      provider: "MoneyGram",
      status: "live",
      source: "MoneyGram Quote API",
      sourceUrl: "https://developer.moneygram.com/moneygram-developer/reference/instructpayouttransactionquote",
      amount,
      base,
      target,
      receivedAmount,
      feeAmount,
      effectiveRate: receivedAmount / amount,
      midMarketRate,
      midMarketTarget,
      deliverySpeed: "Minutes",
      paymentMethod: "Cash pickup",
      riskLabel: "Medium",
      bestFor: "Cash pickup",
      quoteMode: "Live quote ready",
      refreshedAt,
      message: "Live provider quote from backend.",
    });
  } catch {
    return null;
  }
}

function estimatedProviderQuote(
  provider: ProviderCatalogItem,
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
): ProviderQuote {
  const template = quoteTemplates.get(provider.id);
  if (!template) {
    return unavailableQuote(provider.id, base, target, amount, midMarketRate, midMarketTarget, refreshedAt, "Provider is selectable as a wallet or payout rail, but it is not a direct FX quote source.");
  }
  const variableFee = amount * template.feePercent / 100;
  const feeAmount = Math.min(amount, template.fixedFee + variableFee);
  const netSource = Math.max(0, amount - feeAmount);
  const receivedAmount = netSource * midMarketRate * (1 - Math.min(Math.max(template.markupPercent, 0), 99) / 100);
  const status: ProviderQuoteStatus = provider.quoteMode === "Partner API required" ? "partner_setup" : "estimated";
  return completeQuote({
    providerId: provider.id,
    provider: provider.label,
    status,
    source: template.source,
    sourceUrl: template.sourceUrl,
    amount,
    base,
    target,
    receivedAmount,
    feeAmount,
    effectiveRate: amount > 0 ? receivedAmount / amount : 0,
    midMarketRate,
    midMarketTarget,
    deliverySpeed: template.deliverySpeed,
    paymentMethod: template.paymentMethod,
    riskLabel: template.riskLabel,
    bestFor: template.bestFor,
    quoteMode: provider.quoteMode,
    refreshedAt,
    message: status === "partner_setup"
      ? "Partner credentials are required before this provider can be marked live."
      : "FX Always backend estimate until a direct provider API is configured.",
  });
}

function unavailableQuote(
  providerId: string,
  base: string,
  target: string,
  amount: number,
  midMarketRate: number,
  midMarketTarget: number,
  refreshedAt: string,
  message: string,
): ProviderQuote {
  const provider = providerCatalogItems.find((item) => item.id === providerId);
  return completeQuote({
    providerId,
    provider: provider?.label ?? providerId,
    status: "unavailable",
    source: "FX Always provider catalog",
    sourceUrl: "",
    amount,
    base,
    target,
    receivedAmount: 0,
    feeAmount: 0,
    effectiveRate: 0,
    midMarketRate,
    midMarketTarget,
    deliverySpeed: "Unavailable",
    paymentMethod: "Unavailable",
    riskLabel: "High",
    bestFor: "Unavailable",
    quoteMode: provider?.quoteMode ?? "Wallet only",
    refreshedAt,
    message,
  });
}

function completeQuote(input: {
  providerId: string;
  provider: string;
  status: ProviderQuoteStatus;
  source: string;
  sourceUrl: string;
  amount: number;
  base: string;
  target: string;
  receivedAmount: number;
  feeAmount: number;
  effectiveRate: number;
  midMarketRate: number;
  midMarketTarget: number;
  deliverySpeed: string;
  paymentMethod: string;
  riskLabel: string;
  bestFor: string;
  quoteMode: ProviderCatalogItem["quoteMode"];
  refreshedAt: string;
  expiresAt?: string;
  message: string;
}): ProviderQuote {
  const lossAmount = Math.max(0, input.midMarketTarget - input.receivedAmount);
  const lossPercent = input.midMarketTarget > 0 ? lossAmount / input.midMarketTarget * 100 : 0;
  const markupPercent = input.midMarketRate > 0 && input.effectiveRate > 0
    ? Math.max(0, (1 - input.effectiveRate / input.midMarketRate) * 100)
    : 0;
  return {
    providerId: input.providerId,
    provider: input.provider,
    status: input.status,
    source: input.source,
    sourceUrl: input.sourceUrl,
    amount: roundMoney(input.amount),
    sourceCurrency: input.base,
    targetCurrency: input.target,
    receivedAmount: roundMoney(input.receivedAmount),
    feeAmount: roundMoney(input.feeAmount),
    markupPercent: roundPercent(markupPercent),
    lossAmount: roundMoney(lossAmount),
    lossPercent: roundPercent(lossPercent),
    effectiveRate: roundRate(input.effectiveRate),
    deliverySpeed: input.deliverySpeed,
    paymentMethod: titleCase(input.paymentMethod),
    riskLabel: input.riskLabel,
    bestFor: input.bestFor,
    quoteMode: input.quoteMode,
    refreshedAt: input.refreshedAt,
    expiresAt: input.expiresAt ?? new Date(Date.now() + 10 * 60 * 1000).toISOString(),
    message: input.message,
  };
}

async function getMidMarketRate(base: string, target: string): Promise<number> {
  if (base === target) return 1;
  const latest = await getLatestRates(base);
  const rate = latest.rates.find((item) => item.code === target)?.value;
  if (rate && Number.isFinite(rate) && rate > 0) return rate;
  throw new Error(`No mid-market rate for ${base}/${target}`);
}

function selectedQuoteProviderIds(base: string, target: string, requestedProviders: string[], plan: "free" | "pro"): string[] {
  const requested = requestedProviders.length > 0
    ? requestedProviders
    : getProviderCatalog(base).primary.filter((provider) => provider.quoteCapable).map((provider) => provider.id);
  const valid = requested
    .filter((id, index, items) => items.indexOf(id) === index)
    .filter((id) => {
      const provider = providerCatalogItems.find((item) => item.id === id);
      return provider?.quoteCapable === true && provider.currencies.includes(base) && provider.currencies.includes(target);
    });
  return (plan === "free" ? valid.slice(0, 2) : valid).slice(0, 12);
}

function compareProviders(left: ProviderCatalogItem, right: ProviderCatalogItem): number {
  if (right.priority !== left.priority) return right.priority - left.priority;
  return left.label.localeCompare(right.label);
}

function formatDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

function finiteOrZero(value: number | undefined): number {
  return typeof value === "number" && Number.isFinite(value) ? value : 0;
}

function finitePositive(value: number | undefined): number | null {
  return typeof value === "number" && Number.isFinite(value) && value > 0 ? value : null;
}

function finiteOrNull(value: number | undefined): number | null {
  return typeof value === "number" && Number.isFinite(value) ? value : null;
}

function roundMoney(value: number): number {
  return Math.round((Number.isFinite(value) ? value : 0) * 100) / 100;
}

function roundPercent(value: number): number {
  return Math.round((Number.isFinite(value) ? value : 0) * 100) / 100;
}

function roundRate(value: number): number {
  return Math.round((Number.isFinite(value) ? value : 0) * 1_000_000) / 1_000_000;
}

function titleCase(value: string): string {
  return value
    .split(/\s+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function configuredEnv(name: string): string {
  return (process.env[name] ?? "").trim();
}

function firstNumber(value: string, pattern: RegExp): number | null {
  const match = value.match(pattern);
  if (!match?.[1]) return null;
  const parsed = Number.parseFloat(match[1]);
  return Number.isFinite(parsed) ? parsed : null;
}

function clientRequestId(): string {
  return globalThis.crypto?.randomUUID?.() ?? stableId(`${Date.now()}_${Math.random()}`);
}

function comparisonDurationLabel(quote: WiseComparisonQuote): string {
  const maxDuration = quote.deliveryEstimation?.duration?.max ?? quote.deliveryEstimation?.duration?.min;
  if (!maxDuration) return quote.deliveryEstimation?.providerGivesEstimate ? "Provider estimate" : "Market estimate";
  const hours = durationHours(maxDuration);
  if (hours === null) return "Provider estimate";
  if (hours <= 1) return "Minutes";
  if (hours <= 24) return "Same day";
  if (hours <= 48) return "1-2 days";
  return "2+ days";
}

function durationHours(value: string): number | null {
  const match = value.match(/^P(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?)?$/);
  if (!match) return null;
  const days = Number.parseInt(match[1] ?? "0", 10);
  const hours = Number.parseInt(match[2] ?? "0", 10);
  const minutes = Number.parseInt(match[3] ?? "0", 10);
  return days * 24 + hours + minutes / 60;
}

function countryForCurrency(currency: string): string {
  switch (currency) {
    case "ARS": return "ARG";
    case "AUD": return "AUS";
    case "BRL": return "BRA";
    case "CAD": return "CAN";
    case "CHF": return "CHE";
    case "CLP": return "CHL";
    case "COP": return "COL";
    case "EUR": return "ESP";
    case "GBP": return "GBR";
    case "JPY": return "JPN";
    case "MXN": return "MEX";
    case "PEN": return "PER";
    case "USD": return "USA";
    default: return "USA";
  }
}

function cryptoSparkline(currentPrice: number, quote: CoinPaprikaQuote | undefined): number[] {
  const pct24h = finiteOrZero(quote?.percent_change_24h);
  const pct12h = finiteOrZero(quote?.percent_change_12h);
  const pct6h = finiteOrZero(quote?.percent_change_6h);
  const pct1h = finiteOrZero(quote?.percent_change_1h);
  return [pct24h, pct12h, pct6h, pct1h, 0].map((percent) => {
    const divisor = 1 + percent / 100;
    return divisor > 0 ? currentPrice / divisor : currentPrice;
  });
}

function stripExpiry<T extends { expiresAt?: Timestamp }>(value: T): Omit<T, "expiresAt"> {
  const { expiresAt: _, ...rest } = value;
  return rest;
}

function errorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Unknown error";
}
