# Architecture

KMP module structure, Firebase backend, paywall plumbing, offline strategy.

---

## High-level system diagram

```
   ┌─────────────────────────────────────────────────────────┐
   │                  Paid data providers                     │
   │  Open Exchange Rates ($)    CoinGecko Pro ($)            │
   │  ExchangeRate-API ($)       Polygon.io (intraday $)      │
   └────────────────┬────────────────────────────────────────┘
                    │  HTTPS (every 30s, Cloud Scheduler)
                    ▼
   ┌─────────────────────────────────────────────────────────┐
   │                    Firebase project                      │
   │  ┌──────────────────┐    ┌────────────────────────────┐ │
   │  │  Cloud Scheduler │ →  │  Cloud Functions (Node)    │ │
   │  │  every 30s       │    │   fetchLatestRates()       │ │
   │  └──────────────────┘    │   appendCandle()           │ │
   │                          │   purgeOldHistory()        │ │
   │                          └─────────────┬──────────────┘ │
   │                                        ▼                │
   │  ┌────────────────────────────────────────────────────┐ │
   │  │              Firestore (multi-region)              │ │
   │  │  /rates/latest                    (single doc)     │ │
   │  │  /rates/history/{code}/{period}/{bucket}           │ │
   │  │  /news/{date}/{id}                                 │ │
   │  │  /etiquette/{cc}                                   │ │
   │  │  /users/{uid}/{favorites, alerts, settings}        │ │
   │  └────────────────────────────────────────────────────┘ │
   └────────────────┬────────────────────────────────────────┘
                    │
                    │  Firestore SDK (real-time listeners
                    │   + offline cache for free)
                    │
              ┌─────┴─────┐
              ▼           ▼
        ┌────────┐  ┌──────────┐
        │ iOS    │  │ Android  │
        │ (Pro)  │  │ (Free)   │
        └────┬───┘  └──────────┘
             │
             │  StoreKit · subscription
             ▼
        Apple servers
```

---

## Backend — Firebase

### Why Firebase?

1. **Offline cache for free** — Firestore SDK persists the last query result automatically. Your `OfflineScreen` already works; you just check `Source.CACHE` last-updated timestamp.
2. **Single API surface** — KMP can use `firebase-firestore-kmp` (or `gitlive/firebase-kotlin-sdk`) so iOS + Android consume the same code.
3. **Cheap at MVP scale** — under 50k DAU you stay in free/Spark tier. Cloud Scheduler is $0.10/job/month; Functions invocations are cheap.
4. **Auth + subscriptions** — Firebase Auth + StoreKit linkage via Custom Claims is a well-trodden path.

### Firestore schema

```
/rates/
  latest         (doc)
    base: "USD"
    updatedAt: Timestamp
    source: "openexchangerates"
    rates: {
      EUR: { rate: 0.9182, change24h: -0.34, kind: "fiat" }
      GBP: { rate: 0.7841, change24h:  0.12, kind: "fiat" }
      …
      BTC: { rate: 0.0000154, change24h:  2.84, kind: "crypto" }
    }
  history/
    {currencyCode}/   e.g. "EUR"
      {period}/       "1D" | "1W" | "1M" | "1Y" | "ALL"
        {bucket}      e.g. "2026-05" (one doc per month for 1M)
          candles: [{ t: Timestamp, o, h, l, c }]

/news/
  {YYYY-MM-DD}/{auto-id}
    tag: "ECB" | "BoJ" | "FOMC" | "BTC" | …
    impact: "high" | "med" | "low"
    headline: string
    summary: string
    publishedAt: Timestamp
    affectedCurrencies: [{ code: "EUR", change: -0.34 }, …]
    sourceUrl: string

/etiquette/
  {countryCode}  e.g. "JP"
    currencyCode: "JPY"
    tippingRestaurant: { value: 0, label: "not customary · can offend" }
    tax: { included: true, rate: 10 }
    cardsAccepted: ["Visa", "Mastercard", "Suica"]
    preferredPayment: "cash"

/banks/
  {bankId}                       e.g. "wise", "revolut", "chase"
    name: string
    region: ["US", "EU", …]
    feeFormula: {                 # evaluated client-side
      fixedFee: 0,
      percentFee: 0.0042,         # 0.42%
      fxSpread: 0,                # bps above mid-market
    }

/users/{uid}/
  profile: { createdAt, locale, baseCurrency: "USD" }
  favorites: { codes: ["EUR","GBP","JPY","BTC", …] }
  alerts/{alertId}: {
    code: "EUR", op: "<=" | ">=" | "crosses",
    value: 0.91, active: true, createdAt: Timestamp
  }
  subscription: { tier: "free" | "pro", expiresAt: Timestamp, source: "apple" }
```

### Cloud Functions (Node / TypeScript)

```
functions/
├── src/
│   ├── fetchLatestRates.ts      // scheduled · every 30s
│   ├── appendHistory.ts          // scheduled · every 1m for 1D, 1h for 1W/1M
│   ├── purgeOldHistory.ts        // scheduled · daily 03:00 UTC
│   ├── ingestNews.ts             // scheduled · every 5m, pulls from a news API
│   ├── verifyAppleReceipt.ts     // callable · validates StoreKit receipts
│   ├── triggerAlerts.ts          // Firestore trigger on /rates/latest write
│   └── index.ts
├── package.json
└── tsconfig.json
```

### Rate fetch loop — `fetchLatestRates.ts`

Runs every 30 seconds via Cloud Scheduler. Pseudo:

```ts
import { onSchedule } from 'firebase-functions/v2/scheduler';
import { getFirestore, FieldValue } from 'firebase-admin/firestore';

export const fetchLatestRates = onSchedule('every 30 seconds', async () => {
  const [fiat, crypto] = await Promise.all([
    fetchOpenExchangeRates(),     // returns { EUR: 0.9182, GBP: 0.7841, … }
    fetchCoinGecko(['BTC','ETH','SOL']),
  ]);

  const merged = {};
  for (const [k, v] of Object.entries(fiat))
    merged[k] = { rate: v, change24h: await compute24hChange(k, v), kind: 'fiat' };
  for (const [k, v] of Object.entries(crypto))
    merged[k] = { rate: v, change24h: await compute24hChange(k, v), kind: 'crypto' };

  await getFirestore().doc('rates/latest').set({
    base: 'USD',
    updatedAt: FieldValue.serverTimestamp(),
    source: 'openexchangerates+coingecko',
    rates: merged,
  });
});
```

**Why one doc, not many**: every client listens to a single doc. Firestore's pricing is per-read, and one client open = one read per write. With 30s writes and 10k DAU, that's ~28.8M reads/day = well within budget (~$50/month).

### History append — `appendHistory.ts`

Reads the latest doc, opens the right `/rates/history/{code}/{period}/{bucket}` doc, appends to its `candles` array (capped at N entries per bucket — split into a new bucket when full).

Periods → cadence:
- `1D` — 1-minute candles, kept 24h, then rolled into `1W`
- `1W` — 15-minute candles, kept 7 days
- `1M` — 1-hour candles, kept 30 days
- `1Y` — 4-hour candles, kept 365 days
- `ALL` — 1-day candles, kept indefinitely (from 2008)

### Alerts trigger — `triggerAlerts.ts`

Firestore trigger on writes to `/rates/latest`. Reads all alerts where `active=true`, checks each against the new rate; if crossed, sends a push via FCM to the user.

**Trick for scale**: index alerts by currency code (`/alerts-by-code/{code}/{alertId}`) so when EUR moves you only need to query EUR alerts, not all alerts.

---

## KMP — client side

### Module dependencies

```
shared
  ├── ui (depends on data)
  └── data
       ├── platform: firebase
       └── platform: storekit / play-billing (expect/actual)
```

### Repository pattern

```kotlin
interface RatesRepository {
  fun latestFlow(): Flow<Snapshot<Map<String, Rate>>>
  suspend fun history(code: String, period: Period): List<Candle>
  fun isOnline(): Flow<Boolean>
}

class FirebaseRatesRepository(private val firestore: FirebaseFirestore) : RatesRepository {

  override fun latestFlow() = firestore.document("rates/latest")
    .snapshots()
    .map { doc ->
      Snapshot(
        data = doc.toRates(),
        fromCache = doc.metadata.isFromCache,
        updatedAt = doc.get<Timestamp>("updatedAt").toInstant(),
      )
    }
}
```

### Offline detection

```kotlin
val isStale = latestFlow().map { snap ->
  snap.fromCache && Instant.now().toEpochMilli() - snap.updatedAt.toEpochMilli() > 60_000
}
```

If `isStale == true` for more than 30 seconds, swap the current screen to `OfflineScreen` overlay (preserving navigation; tapping retry re-subscribes).

---

## Paid tier — iOS only

### Plumbing

1. App opens, queries StoreKit for available products (`com.yourorg.fx.pro.monthly`, $2.99/mo).
2. User taps "Start FX/ Pro" → StoreKit purchase flow.
3. On success, app sends receipt to `verifyAppleReceipt` callable function.
4. Function validates with Apple's servers, then writes to `/users/{uid}/subscription` and sets a Firebase Custom Claim `tier=pro`.
5. App refreshes ID token. Client gates check `auth.currentUser.tier == "pro"`.
6. Firestore security rules also check `request.auth.token.tier == "pro"` before allowing access to Pro-only data (e.g. extended history beyond 7 days).

### What's gated

| Feature                          | Free          | Pro                        |
| -------------------------------- | ------------- | -------------------------- |
| Live rates (USD-based, top 30)   | ✓             | ✓                          |
| Crypto (top 20)                  | ✓             | ✓                          |
| Converter (basic)                | ✓             | ✓                          |
| History (chart)                  | 1D / 1W       | + 1M / 1Y / ALL            |
| Alerts                           | up to 3       | unlimited                  |
| Fee comparator (real banks)      | locked        | ✓                          |
| Apple Watch + widget             | locked        | ✓                          |
| Traveler mode                    | locked        | ✓                          |
| Offline mode                     | ✓             | ✓                          |
| News                             | ✓             | ✓ + filter by currency     |

**Android is permanently free** — show no paywall, no upgrade prompts. The Android binary should not even include StoreKit / paywall composables.

### Build-flavor split

```kotlin
// shared/build.gradle.kts
android {
  productFlavors {
    create("free")   // both Android and iOS-free builds
    create("paid")   // iOS Pro
  }
}

// In shared/src/iosMain/.../Feature.kt
expect val isPaywallEnabled: Boolean

// shared/src/androidMain/…
actual val isPaywallEnabled: Boolean = false

// shared/src/iosMain/…
actual val isPaywallEnabled: Boolean = true
```

Then in UI:
```kotlin
@Composable
fun ProGate(feature: String, content: @Composable () -> Unit) {
  val tier = LocalSubscriptionTier.current
  if (!isPaywallEnabled || tier == Tier.Pro) {
    content()
  } else {
    PaywallScreen(trigger = feature)
  }
}
```

---

## Cost model (rough)

| Item                          | Cost                                           |
| ----------------------------- | ---------------------------------------------- |
| Open Exchange Rates           | $12/mo (basic plan, 10k req/mo)                |
| CoinGecko Pro                 | $129/mo (analyst plan, 500 req/min)            |
| News API (e.g. NewsAPI.org)   | $0 (free dev) → $449/mo (business)            |
| Firebase Spark (free) tier    | $0 up to 50k DAU                               |
| Cloud Functions               | ~$5/mo at 30s cadence                          |
| Cloud Scheduler               | ~$1/mo                                         |
| Firestore reads/writes        | ~$30–50/mo at 10k DAU                          |
| **Total burn**                | ~$50/mo MVP → ~$600/mo at scale                |
| **Revenue @ 1% conversion**   | 1k Pro users × $2.99 × 70% (Apple cut) = $2,093/mo |

Break-even around 200 paid users. Generous margin after that.

---

## Security

- Firestore rules: clients **read-only** on `/rates/`, `/news/`, `/etiquette/`, `/banks/`. They can only write to `/users/{uid}/`.
- StoreKit receipt validation happens **server-side**. Never trust the client.
- Rates API keys live in Functions env (`firebase functions:config:set`), never in the app.
- News content is server-side translated/summarized (a Function calls an LLM with the source article) so the client never sees attribution issues. Keep the source URL in the doc for click-out.
