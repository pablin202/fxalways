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
- Portfolio basico por watchlist: monto por moneda, valor total en moneda base, cambio diario estimado y peso porcentual.
- Portfolio Pro con average cost, cost basis, P&L realizado/no realizado, allocation fiat/crypto, largest position, chart estimado y transaction history buy/sell.
- Import/export CSV Pro de portfolio con holdings, average cost y transacciones.
- Crypto live catalog con core crypto para Free y catalogo expandido para Pro.
- Comparador de costos estimados por proveedor, monto y par: mejor proveedor, monto recibido, perdida vs mid-market y ahorro potencial.
- Alertas de tipo target rate y daily move.
- Traveler mode con presupuesto local, cash buffer y cheat sheet de gasto.
- Backup/sync de usuario en Android con Firebase.
- Paywall Pro con RevenueCat KMP, restore, Terms/Privacy y configuracion separada Android/iOS.
- Localizacion in-app con selector para 13 idiomas: English, Español, Português, 中文, हिन्दी, Français, العربية, বাংলা, Русский, اردو, Indonesia, Deutsch, 日本語.
- Cache server-side por base currency e historico por par.
- Scheduled refresh cada 60 minutos.
- Firestore cerrado al cliente; la app consume solo HTTPS Functions.
- Configuracion separada para Android/iOS.

## Roadmap competitivo

Para competir contra conversores FX, money transfer apps y trackers crypto modernos, FX Always debe priorizar features que aumenten retencion diaria y valor Pro:

1. Portfolio Pro completo con P&L y allocation. Implementado en primera version.
   - Cash por moneda, crypto holdings y stablecoins.
   - Precio promedio de compra/cost basis.
   - P&L diario, total, realizado/no realizado.
   - Valor total en moneda base.
   - Allocation por asset, tipo de asset y moneda.
   - Grafico estimado de evolucion del patrimonio.
   - Historial de transacciones buy/sell con realized P&L.
2. Smart conversion timing. Implementado en primera version.
   - Score por par: Good time, Wait, Strong rate.
   - Comparacion contra rango 7d/30d/90d, tendencia y volatilidad.
   - Recomendacion concreta para viajeros, ahorro y remesas.
3. Widgets Android/iOS. Android implementado en primera version.
   - Tasa favorita desde el cache live.
   - BTC/ETH cuando estan disponibles en cache.
   - Estado Live/Cache y deep link a la app.
   - Pendiente iOS Widget Extension y portfolio total.
4. Alertas inteligentes.
   - Movimiento de portfolio por porcentaje o monto.
   - Concentracion excesiva en un asset.
   - Rate cerca de maximo/minimo de 30 dias.
   - Mejor proveedor estimado para una conversion.
5. Import/export y backup robusto. CSV portfolio implementado en primera version.
   - CSV import/export de holdings y transacciones.
   - Backup cloud multiplataforma.
   - Historial editable de transacciones.
6. Comparador de costos mas accionable. Resumen principal implementado.
   - Recibiras aproximadamente X despues de fee/spread.
   - Diferencia contra mid-market.
   - Mejor proveedor estimado por monto y par.
   - Ahorro potencial contra el peor proveedor visible.
   - Pendiente: historial de proveedor mas conveniente.
7. Onboarding personalizado.
   - Perfil: viajero, crypto holder, remesas, freelancer, ahorro.
   - Home y alertas sugeridas segun objetivo.

El proximo bloque de producto recomendado es cerrar el historial del comparador y avanzar con onboarding personalizado para mejorar activacion y conversion a Pro.

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
