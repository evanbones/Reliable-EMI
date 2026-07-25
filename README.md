# EMI++

[![neoforge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/neoforge_vector.svg)](https://neoforged.net/)
[![forge](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/forge_vector.svg)](https://files.minecraftforge.net/)
[![fabric](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/supported/fabric_vector.svg)](https://fabricmc.net/)

**EMI++** is an addon for [EMI](https://github.com/emilyploszaj/emi), adding a variety of useful features, improvements,
and customization options to enhance your recipe viewing experience!

## Features

EMI++ provides the following enhancements:

* **Stack Grouping:** Cleans up the EMI item list by grouping related items together (e.g., keeping all colored wools in
  one expandable entry).
* **Creative Mode Tabs:** Displays vanilla and modded Creative Mode tabs directly within the EMI interface for easy
  browsing.
* **Vanilla/Modern Theme:** A unique visual theme for the sidebar, dependent on the EMI theme.

## Configuration

EMI++ offers extensive configuration options to tweak the interface to your needs. You can configure the mod via the
**in-game config screen** with [YACL](https://modrinth.com/mod/yacl) installed or by editing the configuration file
directly.

## Customizing Stack Groups

You can define new custom stack groups or modify existing ones using standard **JSON files**.

EMI++ loads stack groups from the `config/emixx/stack_groups` directory. For example, to create a custom group, create a
JSON file in `config/emixx/stack_groups/my_group.json`.

**JSON Structure:**

| Field        | Type               | Description                                                                                                       |
|--------------|--------------------|-------------------------------------------------------------------------------------------------------------------|
| `id`         | String             | A unique identifier (e.g., `"mypack:currency"`).                                                                  |
| `name`       | String (Optional)  | A translation key or string for the group's name (e.g., `"mypack.group.currency"` or `"Currency"`).               |
| `type`       | String             | Determines how the group is built. Can be `"emixx:group"`, `"emixx:tag"`, or `"emixx:regex"`.                     |
| `enabled`    | Boolean (Optional) | Set to `false` to disable this group.                                                                             |
| `priority`   | Integer (Optional) | Controls match order when an item could belong to multiple groups. Higher values are checked first (default `0`). |
| `contents`   | List               | *(For `emixx:group`)* A list of items or tags to include.                                                         |
| `exclusions` | List               | *(For `emixx:group`)* Items to remove from the group (useful when using broad tags).                              |
| `regex`      | String             | *(For `emixx:group`/`emixx:regex`)* A regular expression pattern used to match item IDs dynamically.              |
| `regexes`    | List (Optional)    | *(For `emixx:group`)* Like `regex`, but accepts a list of multiple patterns.                                      |

**Example: Creating a shiny things group (standard list)**

```json
{
  "id": "mypack:shiny_things",
  "type": "emixx:group",
  "contents": [
    "minecraft:diamond",
    "minecraft:emerald",
    "minecraft:gold_ingot",
    "#c:glass_blocks"
  ],
  "exclusions": [
    "minecraft:purple_stained_glass"
  ]
}

```

**Example: Creating a group using Regex**

You can use the `"emixx:regex"` type to dynamically group items based on their naming patterns. The example below groups
all items that end in _sword (from any namespace):

```json
{
  "id": "mypack:swords",
  "type": "emixx:regex",
  "regex": ".*:.*_sword",
  "name": "Swords"
}

```

**Virtual Objects (Fluids, Modifiers, etc.):**

EMI++ also supports grouping non-item objects such as Fluids, JEED effects, etc. To do this,
prefix the entry with its EMI registry type using the format `<type>:<namespace>:<path>`.

*Example: Creating a special group*

```json
{
  "id": "mypack:basic_fluids",
  "name": "mypack.stack_group.basic_fluids",
  "type": "emixx:group",
  "contents": [
    "fluid:minecraft:water",
    "fluid:minecraft:lava",
    "jeed:effect:minecraft:speed"
  ]
}

```

## Disabling Default Groups

All stack groups are easily togglable using the in-game config screen, in the Stack Groups category.

![I-game stack group config](https://cdn.modrinth.com/data/N9WucjHL/images/a036cab2869e48759d73f3a72ad015d16bef821d.png)

To disable a default stack group, you can also override its definition. Create a file with the same
path/ID as the default group and set `"enabled": false`.

*Example: Disabling the spawn eggs group*

File: `config/emixx/stack_groups/spawn_eggs.json`

```json
{
  "enabled": false
}

```

## License

[![Code license (MIT)](https://img.shields.io/badge/code%20license-MIT-green.svg?style=flat-square)](https://github.com/evanbones/emi-plus-plus/blob/main/LICENSE)
