# Changelog

All notable changes to this project will be documented in this file.

## [0.2.0] - 2026-02-15

### Added

- PDF reader screen with page viewer and swipe navigation
- Fullscreen reader with overlay TopAppBar and FillWidth scaling
- Tap manga in library to open reader
- Reading progress persistence with automatic save (debounced)
- Resume reading from last saved page
- Detekt static analysis with zero-tolerance CI gate
- Instrumented tests in CI and release workflows
- Release signing configuration

### Fixed

- HomeScreenTest assertions after page count model changes

## [0.1.0] - 2026-02-15

### Added

- Homepage with manga library grid layout and cover overlay
- Settings screen
- App logo and "Man AI" branding
- Multi-language support (strings externalized to strings.xml)
- Room database schema for migration validation
- CI pipeline targeting master branch
- Android project scaffold with Clean Architecture and tests
