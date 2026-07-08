# Single Album App

Приложение для воспроизведения музыкального альбома с интерфейсом, адаптированным под Яндекс Музыку.

## 📦 Используемые зависимости

### Основные компоненты Android
- **androidx.core:core-ktx** — Kotlin расширения для Android Core API
- **androidx.appcompat:appcompat** — Поддержка Material Design и совместимость с старыми версиями Android
- **androidx.constraintlayout:constraintlayout** — Гибкая система компоновки UI элементов
- **androidx.activity:activity-ktx** — Kotlin расширения для Activity (viewModels delegate)
- **com.google.android.material:material** — Material Design компоненты (кнопки, темы, стили)

### Архитектура и жизненный цикл
- **androidx.lifecycle:lifecycle-viewmodel-ktx** — ViewModel для хранения данных UI и управления состоянием
- **androidx.lifecycle:lifecycle-livedata-ktx** — LiveData для реактивного обновления UI
- **androidx.lifecycle:lifecycle-runtime-ktx** — Интеграция Coroutines с Lifecycle (viewModelScope)

### Сетевые запросы и парсинг данных
- **com.squareup.okhttp3:okhttp** — HTTP клиент для загрузки JSON с удалённого сервера
- **com.google.code.gson:gson** — Парсинг JSON в Kotlin data classes

### Воспроизведение аудио
- **androidx.media3:media3-exoplayer** — ExoPlayer для воспроизведения MP3 файлов со стримингом
- **androidx.media3:media3-ui** — UI компоненты для плеера (опционально)

### Совместимость
- **desugar_jdk_libs** — Поддержка Java 8+ API (Lambda, Streams) на Android API 23+

---

## 🎯 Обоснование выбора

### Почему OkHttp вместо Retrofit?
Для проекта требуется **только один GET-запрос** для загрузки JSON. Retrofit избыточен — он добавляет лишние зависимости и усложняет код без реальной пользы. OkHttp достаточно для простой загрузки данных.

### Почему ExoPlayer вместо MediaPlayer?
- **Поддержка стриминга** — воспроизведение с удалённых URL без полной загрузки
- **Автоматическое переключение треков** — встроенный callback `onPlaybackStateChanged(STATE_ENDED)`

### Почему ViewModel + LiveData?
- **Разделение ответственности** — логика отделена от UI
- **Переживание конфигурации** — данные сохраняются при повороте экрана
- **Реактивность** — автоматическое обновление UI при изменении состояния плеера
- **Lifecycle-aware** — корректная работа с жизненным циклом Activity

### Почему ConstraintLayout?
- **Плоская иерархия** — нет вложенности, лучше производительность
- **Гибкость** — адаптивная вёрстка для разных размеров экранов
- **GONE-поведение** — автоматическое схлопывание constraints при скрытии элементов (используется для переключения между номером трека и иконкой play/pause)

