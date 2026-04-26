# Rich Text Editor Android App

## Overview
Android rich text editor with MVVM architecture. Supports formatting, images, color highlighting. Data persists in Room database.

## Architecture - MVVM

### Model Layer
- **PostEntity**: Room entity stores documents
```kotlin
@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val title: String,
    val contentJson: String, // Rich content as JSON
    val updatedAt: Long
)
```

- **Repository Pattern**: `PostRepository` + `DefaultPostRepository`
- **Database**: Room SQLite with coroutines
- **Image Storage**: `ImageFileStore` handles image files

### View Layer  
- **Jetpack Compose UI**
- **EditorScreen**: Main editing interface
- **FormatToolbar**: Formatting buttons
- **Material 3 Design**

### ViewModel Layer
- **EditorViewModel**: Core editing logic
- **PostsViewModel**: Document list management
- **State Management**: StateFlow for reactive UI

## Rich Text Features

### 1. Text Formatting
- **Bold, Italic, Underline, Strikethrough**
- **Implementation**: `SpanMutation` class handles all formatting
- **Storage**: Spans converted to JSON via `DocumentJsonCodec`

### 2. Text Colors
- **Foreground Color**: Text color picker
- **Background Color**: Highlight color picker  
- **Implementation**: Color stored as Long in spans

### 3. Image Support
- **Insert images between text**
- **Resize capability**: Width control 80-480dp
- **Storage**: Images saved to app files, referenced by name in text
- **Format**: `[img:filename]` placeholder in text

### 4. Document Structure
- **Blocks**: Text blocks, List blocks, Image blocks
- **Headings**: H1, H2, H3 support
- **Lists**: Ordered/unordered lists

## Data Persistence Strategy

### Rich Text Storage
```kotlin
// Content stored as JSON structure
{
  "blocks": [
    {
      "type": "text",
      "content": "Hello world",
      "spans": [
        {"start": 0, "end": 5, "bold": true}
      ]
    },
    {
      "type": "image", 
      "filename": "image123.jpg",
      "width": 200
    }
  ]
}
```

### Span Management
- **Live Editing**: `SpanMutation` applies formatting to `AnnotatedString`
- **Serialization**: `DocumentJsonCodec` converts spans ↔ JSON
- **Performance**: Character-level span tracking for precise formatting

### Database Schema
```sql
CREATE TABLE posts (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    contentJson TEXT NOT NULL, -- Rich content
    updatedAt INTEGER NOT NULL
);
```

## Technical Implementation

### 1. Formatting Engine (`SpanMutation`)
- **Tiny Array**: Per-character formatting state
- **Span Merging**: Combines overlapping formatting
- **Live Updates**: Real-time formatting during typing

### 2. Block System (`EditorBlock`)
- **Text Blocks**: Rich formatted text
- **Image Blocks**: Embedded images with sizing  
- **List Blocks**: Bullet/numbered lists

### 3. Image Handling
- **Import**: Copy images to app storage
- **Reference**: Store filename in content JSON
- **Display**: Load via file path in Compose

### 4. Auto-Save
- **Debounced**: 800ms delay after changes
- **Background**: Coroutines for non-blocking save
- **State Tracking**: Unsaved changes indicator

## Requirements Compliance

### ✅ Functionality
- [x] Bold, Italic, Underline, Strikethrough
- [x] Image insertion between text
- [x] Image resizing (80-480dp)
- [x] Text color + highlight color
- [x] Database persistence
- [x] Create new documents
- [x] Preview document list
- [x] Delete documents

### ✅ Technical Requirements  
- [x] **MVVM Architecture**: Clear separation of concerns
- [x] **Room Database**: SQLite with coroutines
- [x] **Permissions**: `READ_MEDIA_IMAGES` for image access
- [x] **API 23+ Support**: minSdk = 23
- [x] **Kotlin**: 100% Kotlin codebase
- [x] **Coroutines**: Background operations non-blocking

### ✅ Enhanced Features
- [x] **Dependency Injection**: Hilt for DI
- [x] **Minimal UI**: Clean Material 3 design
- [x] **Navigation**: Compose Navigation between screens
- [x] **State Management**: Reactive UI with StateFlow

## Key Files Structure
```
app/src/main/java/com/example/rich_text_editor/
├── ui/
│   ├── editor/
│   │   ├── EditorScreen.kt        # Main editing UI
│   │   ├── EditorViewModel.kt     # Editing logic
│   │   └── FormatToolbar.kt       # Formatting controls
│   └── posts/
│       ├── PostListScreen.kt      # Document list
│       └── PostsViewModel.kt      # List management
├── editor/
│   ├── SpanMutation.kt           # Formatting engine
│   ├── DocumentJsonCodec.kt      # JSON serialization  
│   └── EditorBlock.kt            # Block data types
├── data/
│   ├── local/
│   │   ├── PostEntity.kt         # Room entity
│   │   ├── PostDao.kt            # Database access
│   │   └── RichEditorDatabase.kt # Database setup
│   └── repository/
│       └── DefaultPostRepository.kt # Data layer
└── di/
    ├── AppModule.kt              # Hilt modules
    └── DatabaseModule.kt
```

## Build Instructions
```bash
# Clone repository
git clone <repository-url>
cd rich-text-editor

# Build debug APK
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

## Performance Optimizations
- **Lazy Loading**: Documents loaded on demand
- **Efficient Spans**: Character-level tracking minimizes memory
- **Debounced Save**: Reduces database writes
- **Image Compression**: Stored in app-specific directory

## Future Enhancements
- Document versioning system
- Export to PDF/HTML
- Collaborative editing
- Cloud sync capability