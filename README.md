# KeSi app by team Absolutne Enote
KeSi is an app for community driven location guessing, based on picture clues, provided by your friends. The app prompts you to take some pictures when leaving your standard location and share them with your friends, that can then try to guess where you are and compete at who is the best at guessing locations based on subtle clues.
You can try the app out yourself by downloading it from our website.

## Team members
[Igor Nikolaj Sok](https://www.linkedin.com/in/igor-nikolaj-sok-767913236/?originalSubdomain=si)
[Vanessa Jačmenjak](https://www.linkedin.com/in/vanessa-ja%C4%8Dmenjak-9a5029202/?originalSubdomain=si)
[Svit Spindler](https://www.linkedin.com/in/svit-spindler-591a7a28b/)
[Jurij Dolenc](https://www.linkedin.com/in/jurijdolenc/)

## Architecture Overview

Kesi is an Android application developed using Kotlin and Java. The application uses Google Maps and Location Services to provide a location-based challenge game. Users can guess locations, submit their guesses, and view their scores on a leaderboard. We use a SaaS backend in back4app, as a NoSQL database. more info about them can be found on their [website](https://www.back4app.com/)

## Features

- **Camera Access:** The application uses the device's camera for certain features.
- **Google Maps Integration:** The application integrates with Google Maps to display locations and allow users to interact with the map.
- **Location Services:** The application uses location services to get the current location of the user.
- **Leaderboard:** The application maintains a leaderboard where users can see their scores.

## Permissions

The application requires the following permissions:

- Camera Access
- Access to Fine and Coarse Location
- Internet Access

## Application Structure

The application is divided into several fragments, governed by a single activity (**MainActivity**):

- **CameraFragment:** This fragment handles the camera functionality of the application and creating new location challenges.
- **EntryFragment:** This fragment handles the display of individual location guessing challenges.
- **ListFragment:** This fragment displays a list of available location guessing challenges.
- **MapFragment:** This fragment handles the Google Maps integration and location-based challenges.

## Dependencies

The application uses the following dependencies:

- Glide: For image loading and manipulation.
- OkHttp: For HTTP requests.
- Google Maps Services: For Google Maps integration.
- Google Location Services: For accessing location data.
- Parse SDK: For backend services.

## Building the Application

The application is built using Gradle. The `build.gradle` file contains the necessary configuration for building the application. Gradle build scripts 7.1.1 are used.

## Running the Application

The application can be run on any Android device running Android 28 (Pie) or above. It can also be run on an emulator in Android Studio.

## Contributing

Contributions to the application as well as ideas for future improvements are welcome. Please ensure that any changes made are well-documented and tested.



