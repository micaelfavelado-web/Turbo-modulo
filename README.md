# Perf Bubble — app Kotlin com bolha flutuante

App companheiro do módulo `TurboPerf-Magisk.zip`. A bolha ⚡ fica sobre qualquer tela;
ao tocar nela aparecem **Ativar turbo** e **Desativar turbo**.

Os comandos são enviados pela **API nativa do AxManager (`axsh`)**, não por HTTP local.
O manifesto nem pede permissão `INTERNET`.

## Como os comandos chegam ao módulo

```
Bolha (app)
   -> AxBroker / Axsh.newProcess("-c", ...)
   -> shell do AxManager (shell ou root)
   -> sh /data/adb/modules/turboperf/action.sh on|off|status
```

## Estrutura

```
perf-bubble-kotlin/
├── settings.gradle.kts
├── build.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/perfbubble/
        │   ├── PerfBubbleApp.kt     # AxBroker.start()
        │   ├── RootBridge.kt        # Axsh -> action.sh
        │   ├── MainActivity.kt      # permissoes e botoes
        │   ├── BubbleService.kt     # bolha + menu flutuante
        │   └── BootReceiver.kt
        └── res/values/
```

## Pré-requisitos

- Android Studio (com Gradle e SDK Android 35).
- JDK 17.
- **AxManager 3.0+** instalado, com o servidor rodando.
- Módulo `TurboPerf-Magisk.zip` já instalado.

## Compilar

1. Abra a pasta `perf-bubble-kotlin` no Android Studio.
2. Aguarde o Gradle sincronizar (ele baixa `com.github.fahrez182.Axeron-API:axsh:3.0.0` do JitPack).
3. `Build > Build APK(s)`.
4. O APK fica em:

```
app/build/outputs/apk/debug/app-debug.apk
```

Pelo terminal:

```sh
./gradlew assembleDebug
```

## Instalar e configurar

1. Instale o APK:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

2. Abra o app **Perf Bubble** uma vez e conceda:
   - **Mostrar sobre outros apps** (sobreposição);
   - **Notificações** (Android 13+);
   - toque em **Permitir acesso no AxManager** e aprove no diálogo do AxManager.
     Também é possível ir em AxManager → Privilege → Perf Bubble → **Allowed**.

3. Toque em **Testar status do módulo**. Deve aparecer algo como:

```
mode=daily
temperature=42C
limit=48C
guard=off
```

4. Toque em **Iniciar bolha**. A bolha ⚡ aparece na tela.

5. Toque na bolha → menu com **Ativar turbo** / **Desativar turbo**.
   Arraste a bolha para reposicionar; toque novamente para fechar o menu.

## Observações

- A permissão do AxManager está ligada ao certificado de assinatura. Se trocar de debug
  para release, será preciso permitir o app novamente.
- Se o sistema bloquear iniciar o serviço após reiniciar, abra o app e toque em
  **Iniciar bolha** (o `BootReceiver` tenta fazer isso automaticamente).
- Se usar outro gerenciador de root (Magisk puro, KernelSU ou APatch), troque apenas
  `RootBridge.kt` pelo adaptador correspondente; o restante do app continua igual.

## Compilar na nuvem (GitHub Actions)

O repositório inclui `.github/workflows/build-apk.yml`. Sempre que você fizer `push` para
`main` ou `master`, o GitHub monta o APK em uma máquina Ubuntu e disponibiliza em:

- **Actions** → clique na execução → **Artifacts** → `perf-bubble-apk`
- **Releases** → APK também é publicado automaticamente (link mais estável)

Assim você não precisa instalar Android Studio no celular ou no computador.
