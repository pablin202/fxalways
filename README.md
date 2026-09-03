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
- Capa de decision comercial: Home recomienda la proxima accion, Convert resume timing + mejor ruta + alerta, y Provider Matrix separa quote live/comparacion/estimado/setup.
- Alertas de tipo target rate y daily move.
- Alertas server-side con FCM para Android y worker local como fallback.
- OCR price scanner para Pro en Android, integrado al chequeo local-vs-live de precios.
- Traveler mode con presupuesto local, cash buffer y cheat sheet de gasto.
- Backup/sync de usuario en Android con Firebase.
- Onboarding personalizado con perfiles Traveler, Crypto holder, Remittances, Freelancer y Savings; aplica pares, watchlist, destino traveler, monto inicial, Home y paywall segun perfil.
- Paywall Pro con RevenueCat KMP, restore, Terms/Privacy y configuracion separada Android/iOS.
- Suscripciones Pro recurrentes mensual/anual; no se expone compra lifetime.
- Rate trust en Home, Convert y Detail: fuente, estado Live/Cache/Preview y hora de actualizacion.
- Loading skeletons para estados lentos de rates, converter y news.
- Analytics basicos: seleccion de plan, compra exitosa, currency agregada y uso de widgets.
- Localizacion in-app con selector para 4 idiomas: English, Español, Português, हिन्दी (los diccionarios de otros 9 idiomas quedan detrás de `EXTENDED_LANGUAGES = false`; `python3 scripts/i18n-audit.py` lista las claves sin traducir).
- Cache server-side por base currency e historico por par.
- Scheduled refresh cada 60 minutos.
- Firestore cerrado al cliente; la app consume solo HTTPS Functions.
- Configuracion separada para Android/iOS.

## Free vs Pro

La fuente de verdad es `composeApp/src/commonMain/kotlin/com/fxalways/app/subscription/FeatureAccessPolicy.kt`. Landing, ficha de Play y paywall copian esta tabla; si cambia una, cambian todas.

| Capacidad | Free | Pro (US$2.99/mes · US$19.99/año) |
|---|---|---|
| Conversor, favoritos, comparador | Monedas ilimitadas | Igual |
| Costo real por proveedor | Tu corredor, 3 proveedores | Todos los proveedores + historial de quién fue más barato |
| Alertas de tasa | 2 activas | Ilimitadas + "mejor momento" y "proveedor más barato hoy" |
| Historial | 1 año | 5 años (all-time) donde exista |
| Cripto | BTC, ETH, USDT, USDC | Catálogo expandido (hasta 200) |
| Portfolio | Holdings básicos | Costo promedio, P&L realizado/no realizado, transacciones, CSV |
| Viajero | 1 destino, reglas locales básicas | Todos los destinos, packs offline, OCR de precios |
| Noticias | Top 3 | Stream completo por región |

Free es generoso en acceso para que la app sea útil antes de pedir Pro. Pro vende profundidad y automatización, no acceso básico.

## Release build (R8)

`release` builds run R8 with `isMinifyEnabled` + `isShrinkResources` and the rules in
`composeApp/proguard-rules.pro` (kotlinx.serialization models, manifest components, enums persisted by
name, Firestore reflection, ML Kit / CameraX, RevenueCat, Ktor). The Crashlytics Gradle plugin uploads
`mapping.txt` on every release build and CI also attaches it as the `android-release-mapping-<versionCode>`
artifact; the AAB carries the deobfuscation file for Play automatically.

Without a release keystore the release build type is signed with the debug key so a minified APK can be
installed locally: `./gradlew :composeApp:assembleRelease` then `adb install -r composeApp/build/outputs/apk/release/composeApp-release.apk`.
When adding a new `@Serializable` model or a class reached via reflection, smoke-test the release build on a device.

## Borrado de cuenta

Settings → Backup → **Delete account** (disponible también para invitados). Tras confirmar, la app borra en Firestore `users/{uid}` con sus subcolecciones (`backups`, `push_tokens`, `server_alert_events`), elimina el usuario de Firebase Auth (reautenticando con Google si hace falta), cierra la sesión de RevenueCat, limpia todas las preferencias locales y vuelve al onboarding. Las compras quedan en la cuenta de Google. La URL de borrado declarada en Play Console debe apuntar a una página de fxalways.com que describa estos pasos.

## Roadmap competitivo

Benchmark detallado y roadmap por mercado: [PRODUCT_BENCHMARK_ROADMAP.md](PRODUCT_BENCHMARK_ROADMAP.md).

Para competir contra conversores FX, money transfer apps y trackers crypto modernos, FX Always debe priorizar features que aumenten retencion diaria y valor Pro:

Posicionamiento actual: FX Always es una decision layer de FX, no un money transmitter. La app muestra mid-market, fees/spread estimados, provider status, alertas y contexto para decidir mejor antes de convertir, pagar o enviar. Claims recomendados para screenshots de Play: "Know when to convert", "Compare real provider cost", "Scan prices abroad", "Get push alerts", "Track your multi-currency money".

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

El bloque P0 quedo implementado y probado: fee/spread reality check, smart alerts con historial, travel offline pack, watchlist groups y expansion de widgets Android con estado de cache.

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
./gradlew :composeApp:assembleDebug -PFX_BACKEND_URL=https://us-central1-fx-always.cloudfunctions.net
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

### Release automatico en GitHub

Cada push a `main` ejecuta:

1. Build Android debug + APK de instrumented tests.
2. Firebase Test Lab en SmallPhone, MediumPhone y MediumTablet.
3. Si Test Lab pasa, build del AAB release firmado para Play.
4. Publicacion del AAB y su `.sha256` en GitHub Releases.

La version automatica usa:

- `versionCode = github.run_number`
- `versionName = 1.0.<github.run_number>`
- tag `android-v1.0.<github.run_number>`

Secrets requeridos en GitHub:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`
- `REVENUECAT_API_KEY`

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

### Firebase Test Lab en CI

Cada push a `main` ejecuta todos los UI tests Android en Firebase Test Lab. El workflow:

- Compila el APK debug y el APK `androidTest`.
- Consulta el catalogo actual de Test Lab con `gcloud firebase test android models list`.
- Selecciona 3 devices disponibles con distintas familias/resoluciones cuando existen en el catalogo.
- Corre `gcloud firebase test android run` como instrumentation test, sin `test-targets`, para ejecutar toda la suite.

Configuracion requerida en GitHub:

- Repository variable `FIREBASE_TEST_LAB_PROJECT`: Firebase/GCP project id. Default actual: `fx-always`.
- Repository variable `FIREBASE_TEST_LAB_RESULTS_BUCKET`: Cloud Storage bucket para resultados. Default actual: `fx-always-test-lab-results`.
- Repository secret `GCP_WORKLOAD_IDENTITY_PROVIDER`: provider de Workload Identity Federation para GitHub Actions.
- Repository secret `GCP_SERVICE_ACCOUNT`: service account usado por el workflow.

El service account necesita permisos para ejecutar Firebase Test Lab y escribir resultados en Cloud Storage. En GCP, asignar permisos equivalentes a Firebase Test Lab Admin para el proyecto y Storage Object Admin sobre el bucket de resultados que use Test Lab. APIs requeridas: `testing.googleapis.com` y `toolresults.googleapis.com`.

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
- `GET /providerCatalog?base=AUD`
- `GET /providerQuotes?base=AUD&target=ARS&amount=500&providers=wise,revolut,moneygram&plan=pro`

Provider quotes run through Firebase Functions so provider credentials stay off-device. Wise uses the official quote endpoint when reachable and Wise Comparison API for market comparison data across supported providers. Revolut and MoneyGram switch to live mode when `REVOLUT_API_TOKEN`, `MONEYGRAM_ACCESS_TOKEN` and `MONEYGRAM_PARTNER_ID` are configured in the backend environment; otherwise the endpoint returns explicit `comparison`, `partner_setup` or `estimated` statuses with source labels. Provider Matrix copy treats those states separately so production UI never implies an estimate or comparison is a locked provider quote.

Server-side alert evaluation runs as the scheduled `evaluateServerAlerts` Firebase Function every 15 minutes. It reads backed-up alert snapshots, evaluates target-rate and daily-change alerts against backend rates, writes `lastTriggeredAtMillis` back to the backup payload, records server alert events under each user and sends FCM push notifications to registered Android devices. Android also keeps the local alert worker as a fallback path.

## Deploy Firebase

Production project: `fx-always` (project number `75079929673`, Firestore in `nam5`, Functions in `us-central1`). Migrated from the shared `moneytrackerpro-8ff64` project on 2026-09-03 (issue #23).

```bash
firebase use fx-always
cd functions
npm install
npm run deploy
```

Secrets: `MARKETAUX_API_KEY` must exist in Secret Manager for the deploy to succeed. The literal value `unset` is a documented placeholder: `newsFeed` then falls back to GDELT. Set the real key with:

```bash
printf '%s' "$MARKETAUX_KEY" | firebase functions:secrets:set MARKETAUX_API_KEY --project fx-always --data-file=-
```

CI (Firebase Test Lab) authenticates through Workload Identity Federation: pool `github-pool`, provider `github-provider`, service account `github-ci@fx-always.iam.gserviceaccount.com`, results bucket `gs://fx-always-test-lab-results`. The GitHub secrets `GCP_WORKLOAD_IDENTITY_PROVIDER` / `GCP_SERVICE_ACCOUNT` and variables `FIREBASE_TEST_LAB_PROJECT` / `FIREBASE_TEST_LAB_RESULTS_BUCKET` point there.

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
