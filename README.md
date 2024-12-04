# The Armory

The Armory is a highly customizable and scalable Discord bot that provides robust integrations and utility-focused features for your Discord server. Designed with modern practices, it leverages Discord's slash commands for smooth interaction and efficient management.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Setup](#setup)
- [Usage](#usage)
- [Configuration](#configuration)
- [Contributing](#contributing)
- [Documentation](#documentation)
- [License](#license)
- [Contact](#contact)

## Features

- **Slash Commands**: Fully operates with Discord's slash command system for modern user interaction.
- **Integration with External APIs**: Provides integrations for advanced functionalities, enabling seamless expansion.
- **Music Playback**: Powered by Lavalink for high-quality audio streaming.
- **Utility Commands**: A suite of useful commands designed to enhance the experience for server members.
- **Scalability**: Built to handle a wide range of server sizes with Docker-based deployment.

## Installation

### Prerequisites

- **Docker**: Required for containerized deployment.
- **Discord Bot Account**: Set up via the [Discord Developer Portal](https://discord.com/developers/applications).
- **API Keys/Secrets**: Ensure you have access to all required secrets for integrations (see [Configuration](#configuration)).

### Setup

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Horeak/TheArmoryPublic.git
   cd TheArmoryPublic
   ```

2. **Set Up Secrets**:

   Ensure all required secrets (e.g., tokens, API keys) are configured in your environment. You can use a `.env` file for local development or set them up as Docker secrets for production.

3. **Build and Run with Docker**:

   ```bash
   docker-compose up --build
   ```

   This command builds the Docker image and starts the bot.

## Usage

After deployment:

1. **Invite the Bot**:
   Use the OAuth2 URL generated in the Discord Developer Portal to invite the bot to your server.
2. **Use Slash Commands**:
   Access the bot's features via Discord's slash command interface. Type `/` in your server to see the available commands. For more details, refer to the documentation.

## Configuration

The bot is configured using environment variables, which should include necessary API keys, tokens, and other sensitive information. These can be managed using a `.env` file during local development or passed as secrets in a Docker environment. Ensure proper security for sensitive data.

## Contributing

We welcome contributions to The Armory! To contribute:

1. **Fork the Repository**:
   Click the "Fork" button on GitHub.
2. **Clone Your Fork**:

   ```bash
   git clone https://github.com/your-username/TheArmoryPublic.git
   ```

3. **Create a Feature Branch**:

   ```bash
   git checkout -b feature/YourFeatureName
   ```

4. **Make Your Changes**: Ensure they follow the project guidelines.
5. **Commit Changes**:

   ```bash
   git commit -m "Add YourFeatureName"
   ```

6. **Push Your Branch**:

   ```bash
   git push origin feature/YourFeatureName
   ```

7. **Submit a Pull Request**:
   Open a pull request on the main repository for review.

## Documentation

For detailed documentation, refer to the following resources:

- [Command overview](https://github.com/Horeak/TheArmory/blob/master/Bot/docs/commands-internal.md)

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact

For support, questions, or feedback, please open an issue on this repository.
