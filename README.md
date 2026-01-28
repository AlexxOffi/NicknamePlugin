# Hytale Plugin Template

A template for Hytale java plugins. Created by [Up](https://github.com/UpcraftLP), and slightly modified by Kaupenjoe.

# Nickname Plugin

Started off forking https://github.com/Corin-alt/HytaleNickNameMod
v1.0.0 offered a simple nickname functionality

# v1.1.0 LuckPerms integration

v1.1.0 offers a full LuckPerms Chat Formatter integration.
To enable it, you first need to disable LuckPerms own formatter in the config
(LuckPerms_LuckPerms/config.yml -> chat-formatter.enabled: false)

When starting the server with HyNickname, it automatically created a folder called "Offi_HyNickname".
In the config.yml, you can enable/disable the formatter. It's enabled by default.
You also find a "forbiddennames.yml" in which you can add words or patterns you don't want users to use.

# Permissions

To allow a user to use the Nickname functionality, he requires the permission
"nickname.use"

There's also the option to remove another user's nickname, which requires
"nickname.admin.reset"

# Usage

To set one's own nickname, run "/nick [name]"
To reset one's own nickname, run "/nick reset"

For admins: To reset someone else's nickname, run "/nick reset --target=[name]"
