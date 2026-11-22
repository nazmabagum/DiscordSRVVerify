
# DiscordSRVVerify

A DiscordSRV addon that allows Minecraft players to verify their accounts using a Discord slash command.

## Features

- `/verify code:<code>` Discord slash command
- Private (ephemeral) responses
- Uses DiscordSRV's existing account-linking system
- No separate Discord bot
- No DM verification required
- Guild-specific command registration

## Requirements

- Paper 26.2
- DiscordSRV 1.30.5
- Java 21 or compatible version required by the server

## Configuration

The Discord server ID is:

`1527000186734252192`

## Verification

1. Generate a linking code in Minecraft.
2. Run `/verify` in Discord.
3. Enter your linking code.
4. DiscordSRV processes the account link.
