# Jimmer Example Android Chat

This is a simple Android Chat application that demonstrates how to use Jimmer with Android.

## 📱 Features

- **Real-time Messaging**: UDP-based chat server for instant message delivery
- **Contact Management**: Add, view, and manage chat contacts
- **Modern UI**: Built with Jetpack Compose and Material 3 design
- **Local Database**: SQLite with Jimmer ORM for data persistence
- **Network Discovery**: Find and connect with other users on the network
- **Responsive Design**: Adaptive layout for different screen sizes

## 🏗️ Architecture

### Tech Stack
- **UI Framework**: Jetpack Compose with Material 3
- **Database**: SQLite with Jimmer ORM
- **Networking**: Netty UDP server/client
- **Dependency Injection**: Koin
- **Navigation**: Jetpack Navigation Compose
- **Language**: Kotlin

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 36 (API level 36)
- Minimum SDK: 26 (Android 8.0)
- JDK 11

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Enaium/jimmer-example-android-chat.git
   cd jimmer-example-android-chat
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build and Run**
   - Connect an Android device or start an emulator
   - Click the "Run" button (green play icon) in Android Studio
   - Or use the command line:
   ```bash
   ./gradlew assembleDebug
   ```

### Configuration

The app uses UDP networking on port 8888. Make sure your device has network permissions and can communicate on the local network.

## 📖 Usage

### Main Interface
The app features a bottom navigation with two main tabs:

1. **Chats Tab**: View all your conversations
   - Tap on a conversation to open the chat
   - Messages are displayed in chronological order
   - Send new messages using the text input at the bottom

2. **Contacts Tab**: Manage your contacts
   - View all saved contacts
   - Tap on a contact to see details
   - Add new contacts through the search functionality

### Finding Contacts
- Tap the search icon in the top app bar
- The app will scan the network for other users
- Select a contact to start chatting

### Chat Features
- **Real-time messaging**: Messages are sent and received instantly
- **Message history**: All conversations are saved locally
- **Contact information**: View contact details and message count

## 📱 Screenshots

![20250706230016](https://s2.loli.net/2025/07/06/X6SVwPue7R1LcFj.png)
![20250706230028](https://s2.loli.net/2025/07/06/pKB5lWC4OGRcbgi.png)
![20250706231012](https://s2.loli.net/2025/07/06/agfHvtonYhNC7Z8.png)
![20250706231032](https://s2.loli.net/2025/07/06/COqu17GaoMADrFl.png)
![20250706230201](https://s2.loli.net/2025/07/06/wd28mrFxQXZWIGR.png)
![20250706230215](https://s2.loli.net/2025/07/06/fSErao37Xp6zFTN.png)
![20250706230329](https://s2.loli.net/2025/07/06/8rkJmTxEzGR49SZ.png)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- [Jimmer ORM](https://github.com/babyfish-ct/jimmer) for the database layer
- [Netty](https://netty.io/) for networking capabilities
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI development
- [Koin](https://insert-koin.io/) for dependency injection