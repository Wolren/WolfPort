<div align="center">

![Wolf Port](docs/icon.png)

# Wolf Port

Replaces classic wolves with the 24w10a+ variants and gives tamed wolves more health. No other features.

[![License](https://img.shields.io/github/license/Wolren/WolfPort)](LICENSE)
[![Last commit](https://img.shields.io/github/last-commit/Wolren/WolfPort)](https://github.com/Wolren/WolfPort/commits)
[![Issues](https://img.shields.io/github/issues/Wolren/WolfPort)](https://github.com/Wolren/WolfPort/issues)
[![Code size](https://img.shields.io/github/languages/code-size/Wolren/WolfPort)]()
[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](build.gradle)
[![Fabric](https://img.shields.io/badge/Fabric-1.20.1-blue?logo=fabric)](build.gradle)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green?logo=minecraft)](https://minecraft.net)
[![Modrinth](https://img.shields.io/modrinth/v/wolfport?label=Modrinth&logo=modrinth)](https://modrinth.com/mod/wolfport)
[![OpenSSF Scorecard](https://api.securityscorecards.dev/projects/github.com/Wolren/WolfPort/badge)](https://securityscorecards.dev/viewer/?uri=github.com/Wolren/WolfPort)
[![Socket](https://img.shields.io/badge/Socket-Supply%20Chain%20Security-333?logo=socketdotdev)](https://socket.dev)

</div>

## Features

- Replaces the classic wolf with the 9 variants added in 24w10a+: pale, spotted, snowy, black, ashen, rusty, woods, chestnut, striped
- Wolves spawn with the variant of their biome, matching the vanilla mapping
- Tamed wolves get 40 health (20 hearts); untamed wolves get 8 (4 hearts). Vanilla has 20 for both
- Nothing else changes: no new items, blocks, or mechanics

## Installation

Download the jar for your Minecraft version from [Modrinth](https://modrinth.com/mod/wolfport) and drop it into your `mods` folder. Fabric API is required.

Available for Minecraft 1.16.5 through 1.20.4 on Fabric and Quilt.

## Compatibility

- Compatible with Woodwalkers
- For versions below 1.20, consider the "Disable Custom Worlds Advice" mod

## FAQ

**Will you add wolf armor or other *24w10a+* functionality?**

No. If heavily requested, that would ship as a separate mod.

**Is this compatible with mods adding utilities for wolves?**

Yes. WolfPort directly modifies the `WolfEntity` class, so everything except other mods adding new variants or using mixins on the class should be compatible.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

[MIT](LICENSE)
