# FX Always

Kotlin Multiplatform + Compose app para currency exchange con rates actuales, historicos, watchlist global, crypto, portfolio, alertas, traveler mode y suscripciones Pro con RevenueCat.

## Arquitectura

- `composeApp`: UI compartida Compose para Android/iOS.
- `functions`: Firebase Cloud Functions TypeScript.
- Firestore: cache backend de `latest` y `history` para evitar miles de llamadas al proveedor FX.
- Proveedor FX inicial: Frankfurter API, basada en datos del European Central Bank.
- Premium: `SubscriptionGateway` conectado a RevenueCat KMP para Android/iOS.
- Android release: AAB firmado para Google Play Internal Testing y versionCode automatizable por CI.

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
- Onboarding personalizado con perfiles Traveler, Crypto holder, Remittances, Freelancer y Savings; aplica pares, watchlist, destino traveler, monto inicial, Home y paywall segun perfil.
- Paywall Pro con RevenueCat KMP, restore, Terms/Privacy y configuracion separada Android/iOS.
- Suscripciones Pro recurrentes mensual/anual; no se expone compra lifetime.
- Rate trust en Home, Convert y Detail: fuente, estado Live/Cache/Preview y hora de actualizacion.
- Loading skeletons para estados lentos de rates, converter y news.
- Analytics basicos: seleccion de plan, compra exitosa, currency agregada y uso de widgets.
- Localizacion in-app con selector para 13 idiomas: English, Español, Português, 中文, हिन्दी, Français, العربية, বাংলা, Русский, اردو, Indonesia, Deutsch, 日本語.
- Cache server-side por base currency e historico por par.
- Scheduled refresh cada 60 minutos.
- Firestore cerrado al cliente; la app consume solo HTTPS Functions.
- Configuracion separada para Android/iOS.

## Free vs Pro

Free esta pensado para validar valor diario sin bloquear lo esencial:

- Core FX/crypto inicial.
- Conversor, Home, traveler basics y alertas basicas.
- Crypto core: BTC, ETH, USDT y USDC.
- Historial corto y previews Pro donde corresponde.

Pro aumenta profundidad y retencion:

- Catalogo crypto expandido.
- Portfolio completo con holdings, average cost, P&L y transacciones.
- Historial largo.
- Import/export CSV.
- Alertas avanzadas.
- Comparaciones y sugerencias mas completas.
- Mas personalizacion por perfil.

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
7. Onboarding personalizado. Implementado en primera version.
   - Perfil: viajero, crypto holder, remesas, freelancer, ahorro.
   - Home, pares iniciales, watchlist, destino traveler, monto sugerido, paywall y alertas sugeridas segun objetivo.

El proximo bloque de producto recomendado es cerrar el historial del comparador y convertir las alertas sugeridas por perfil en creacion one-tap.

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

## Android release para Play

El AAB de Play se genera desde:

```bash
./gradlew :composeApp:bundleRelease -PANDROID_VERSION_CODE=5 -PANDROID_VERSION_NAME=1.0.2
```

Salida:

```text
composeApp/build/outputs/bundle/release/composeApp-release.aab
```

Release signing se configura por `local.properties`, propiedades Gradle o environment variables:

```properties
ANDROID_KEYSTORE_PATH=/absolute/path/to/fxalways-upload.jks
ANDROID_KEYSTORE_PASSWORD=...
ANDROID_KEY_ALIAS=fxalways
ANDROID_KEY_PASSWORD=...
REVENUECAT_API_KEY=goog_...
```

No commitear keystores ni passwords. El repo ignora `*.jks`, `*.keystore`, `release-key.properties`, `keystore.properties` y `local.properties`.

### VersionCode automatico

`composeApp/build.gradle.kts` lee:

- `ANDROID_VERSION_CODE`
- `ANDROID_VERSION_NAME`

Prioridad: Gradle property, `local.properties`, environment variable, default del repo.

Ejemplo manual:

```bash
./gradlew :composeApp:bundleRelease -PANDROID_VERSION_CODE=5 -PANDROID_VERSION_NAME=1.0.2
```

GitHub Actions usa `github.run_number` como `ANDROID_VERSION_CODE`, por lo que los artefactos de CI tienen codigo creciente automaticamente.

### Advertencias de Play Console

- `No hay archivo de desofuscacion`: normal mientras `isMinifyEnabled=false`. No hay `mapping.txt` porque no se usa R8/ProGuard en release.
- `Codigo nativo sin simbolos`: la app incluye `.so` de dependencias. Release configura `ndk.debugSymbolLevel = "SYMBOL_TABLE"`, pero algunas dependencias pueden venir ya strippeadas. Es advertencia, no bloqueo para Internal Testing.

## RevenueCat y compras Android

Configuracion esperada en RevenueCat:

- Entitlement: `pro`.
- Offering: `pro` recomendado. La app tambien acepta `default` o el current offering como fallback.
- Packages:
  - `$rc_monthly` -> `fxalways_pro_monthly`
  - `$rc_annual` -> `fxalways_pro_annual`
- Android SDK key: `goog_...`.
- No usar lifetime package.

Configuracion esperada en Google Play:

- App package: `com.fxalways.app`.
- Subscriptions/product IDs exactos:
  - `fxalways_pro_monthly`
  - `fxalways_pro_annual`
- Internal testing track con AAB firmado.
- Tester agregado al track interno y tambien a license testing para compras sandbox.

Importante: compras reales/sandbox no se validan instalando por `adb` o `bundletool`. Si el dispositivo muestra `installerPackageName=null`, Google Billing puede responder como no disponible y el paywall puede mostrar `Purchases unavailable`. Para probar compras, instalar desde el opt-in link de Google Play Internal Testing; el installer debe ser `com.android.vending`.

## Internal Testing checklist

1. Generar AAB firmado con versionCode nuevo.
2. Subirlo a Google Play Console -> Testing -> Internal testing.
3. Completar los formularios obligatorios: store listing, data safety, app content, privacy policy, categoria y contacto.
4. Confirmar productos de suscripcion activos en Play y conectados a RevenueCat.
5. Agregar testers al track interno y a license testing.
6. Instalar desde el link interno de Play.
7. Probar:
   - Onboarding completo.
   - Home rates y crypto core.
   - Paywall mensual/anual.
   - Compra sandbox mensual.
   - Restore.
   - Free vs Pro gating.
   - Links Terms/Privacy.
   - Offline/cache states.
   - Widgets.

## Troubleshooting rapido

- `Todos los bundles subidos deben estar firmados`: falta `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` o `ANDROID_KEY_PASSWORD`.
- `Version code already used`: subir `ANDROID_VERSION_CODE` y regenerar el AAB.
- `Purchases unavailable` instalado localmente: esperado con `adb`; instalar desde Play Internal Testing.
- Crash post-onboarding por compras: RevenueCat ya soporta arranque anonimo si Firebase UID todavia no esta disponible.
- Crash al abrir Terms/Privacy: Android ahora captura `ActivityNotFoundException`; si no hay navegador muestra `No browser available`.

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
