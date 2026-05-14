# FX Always

Kotlin Multiplatform + Compose app para currency exchange con rates actuales, historicos, watchlist global y arquitectura lista para cobrar suscripcion mensual en iOS.

## Arquitectura

- `composeApp`: UI compartida Compose para Android/iOS.
- `functions`: Firebase Cloud Functions TypeScript.
- Firestore: cache backend de `latest` y `history` para evitar miles de llamadas al proveedor FX.
- Proveedor FX inicial: Frankfurter API, basada en datos del European Central Bank.
- iOS premium: `SubscriptionGateway` conectado a RevenueCat KMP con Test Store durante desarrollo.

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

## iOS

El proyecto iOS se genera con XcodeGen y compila el framework Compose correcto para device o simulador desde Xcode.

```bash
./scripts/ios-simulator-run.sh
./scripts/ios-device-run.sh
```

Para produccion iOS todavia falta:

1. Crear la app iOS en Firebase y agregar `GoogleService-Info.plist`.
2. Habilitar Sign in with Apple en Apple Developer y Firebase Auth.
3. Cambiar la key RevenueCat Test Store por la public SDK key de App Store.

## Fuentes revisadas

- Frankfurter API: https://frankfurter.dev/
- Firebase scheduled functions: https://firebase.google.com/docs/functions/schedule-functions
- RevenueCat KMP subscriptions: https://www.revenuecat.com/blog/engineering/cmp-subscriptions/
