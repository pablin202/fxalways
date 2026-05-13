# FX Always

Kotlin Multiplatform + Compose app para currency exchange con rates actuales, historicos, watchlist global y arquitectura lista para cobrar suscripcion mensual en iOS.

## Arquitectura

- `composeApp`: UI compartida Compose para Android/iOS.
- `functions`: Firebase Cloud Functions TypeScript.
- Firestore: cache backend de `latest` y `history` para evitar miles de llamadas al proveedor FX.
- Proveedor FX inicial: Frankfurter API, basada en datos del European Central Bank.
- iOS premium: capa `SubscriptionGateway` lista para reemplazar por RevenueCat KMP.

## Features incluidas

- Conversor multi-currency.
- Watchlist con pares mayores.
- Historico 12 meses gratis y 5 anos para Pro.
- Sparkline por par.
- Localizacion in-app con selector para 13 idiomas: English, Español, Português, 中文, हिन्दी, Français, العربية, বাংলা, Русский, اردو, Indonesia, Deutsch, 日本語.
- Cache server-side por base currency e historico por par.
- Scheduled refresh cada 60 minutos.
- Firestore cerrado al cliente; la app consume solo HTTPS Functions.
- Configuracion separada para Android/iOS.

## Ejecutar Android

El backend publicado queda configurado por default:

```bash
./gradlew :composeApp:assembleDebug
```

Para usar el emulador Firebase local:

```bash
./gradlew :composeApp:assembleDebug -PFX_BACKEND_URL=http://10.0.2.2:5001/demo-fx-always/us-central1
```

Para usar Firebase deployado:

```bash
./gradlew :composeApp:assembleDebug -PFX_BACKEND_URL=https://us-central1-moneytrackerpro-8ff64.cloudfunctions.net
```

## Ejecutar backend

```bash
cd functions
npm install
npm run build
cd ..
firebase emulators:start --only functions,firestore
```

Endpoints:

- `GET /latestRates?base=USD`
- `GET /historicalRates?base=USD&quote=EUR&days=365`

## Deploy Firebase

```bash
firebase use moneytrackerpro-8ff64
cd functions
npm install
npm run deploy
```

## Suscripcion iOS

La app incluye `SubscriptionGateway` para aislar pagos del resto del producto. El placeholder deja Android libre y bloquea features Pro en iOS.

Implementacion recomendada:

1. Crear suscripcion mensual auto-renovable en App Store Connect.
2. Crear entitlement `pro` y offering mensual en RevenueCat.
3. Reemplazar `PlaceholderSubscriptionGateway` por un gateway RevenueCat KMP.
4. Usar `PlatformConfig.revenueCatApiKey` para inicializar RevenueCat por plataforma.

## Fuentes revisadas

- Frankfurter API: https://frankfurter.dev/
- Firebase scheduled functions: https://firebase.google.com/docs/functions/schedule-functions
- RevenueCat KMP subscriptions: https://www.revenuecat.com/blog/engineering/cmp-subscriptions/
