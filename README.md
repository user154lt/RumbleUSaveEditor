# Pokemon Rumble U save editor

A simple save editor for Pokémon rumble U written in Kotlin using Jetpack Compose

Available for Windows, Linux and macOS. 

Release formats:
exe, msi, deb, dmg, jar

jar files require jdk 18+ to be installed. All releases are 64-bit, but 32-bit binaries can be built by cloning this
repo and building on a 32-bit machine.

**IMPORTANT:**

Please backup your save before using this (or any) save editor!

![screenshot](https://imgur.com/tDnWM5s)

![screenshot](https://imgur.com/f2RqqKo)

The compressed save files follow the same structure as the save files for Pokémon Rumble world 
as documented [here](https://gbatemp.net/threads/pokemon-rumble-world-diamonds-p-tool.386526/) by 
[ScriresM](https://github.com/SciresM). The first 7 bytes are the `cAVIAR2` signature, at offset `0x08` is the CRC32 of 
the compressed data. At `0x28` a value of `0x01` indicates that the data is compressed. The 4 bytes from `0x2C - 0x2F` 
are the decompressed size. Following this is the ZLib stream compressed with best compression (level 9) as indicated by
the 0x78DA signature.

Everything from `0x0C - 0x27` in the header seems to be ignored when being parsed by the game. It is possible to put
whatever data you like in here and the file will still be parsed as normal by the game. Sadly it does not seem to be 
possible to append any extra data to the end of the file. Seeing as zip files use deflate, which is just a ZLib stream
stripped of its header and the Adler32 footer, you can craft a zip file with the compressed stream. You just need to
decompress the stream first so that you can calculate the CRC32 of the decompressed data. This zip can then be
decompressed with any standard zip software to give a copy of the decompressed save file. This is just a bit of fun
though, it doesn't serve much purpose seeing as it requires decompressing the data beforehand.

The files `00pb0*.dat` contain Pokémon data when decompressed. Each box stars with the signature `02 85 64 C0 04 82`.
Each Pokémon slot in the box is 67 bytes long, it starts with `02 3E` and ends with `04 82 00 00`. If a slot is
populated the bytes at offsets `0x0E, 0x10` and `0x12` will all be `0x3F`. The species, moves, and power modifier are 
all 2 bytes long. The species is at offset `0x2D`, followed immediately by the A move, the B move and then finally the 
power modifier. The number for the species for the most part is simply the Pokédex number * 4, a couple of Pokémon's 
legit data has an extra 1 or 2 added to this number afterward however the game is happy to parse data for these Pokémon 
when stored as a multiple of 4, however I will stress again the importance of making a backup before editing your save. 
The Pokémon I have noticed so far seem to be some Pokémon that have additional forms and Weepinbell. Moves are simply 
stored as their number, a list of moves can be found in [PokemonDataProvider.kt](src/main/kotlin/data/PokemonDataProvider.kt).
Power modifier, as I have referred to it, does not represent the actual power of a Pokémon. It is a value that is
exponentially related to the power, at it's maximum of 65535 the actual power will be just over 10000000. The byte at
offset `0x35` is for the trait. Editing this seems to have no real effect. The various traits are just Gift apart from
in the Japanese version where they correspond to the name of an outlet that released a password for the game. There are
also some blank placeholder values that seem to do nothing at all. Anything higher than `0x30` causes the game to 
consider the Pokémon to be corrupted. If the game detects invalid Pokémon data in a box then it will not load anymore 
Pokémon from the box and will move onto the next one.

This tool does also decompress `00main.dat` but does not do anything with it. I have looked at the main file briefly
but not very much, if I look into it and find anything significant I may update the tool to reflect this at a later 
date.

## Building:

Clone the repo and build in Intellij. The project can be built for Windows, Linux or macOS. As this is a Jetpack Compose
project, cross-platform builds are not possible. You can only build binaries for the host platform.


## Special thanks:

I need to say a huge thank you to [ScriresM](https://github.com/SciresM). It was thanks to his work on Pokémon rumble
world that I knew it would be possible to decompress these save files in the same way, also his tool has brought
countless hours of enjoyment to my son. 

Thanks also go to [Serebii](https://www.serebii.net/) for their Pokémon lists which were incredibly helpful when making
this tool.

Thanks to [Xelu](https://thoseawesomeguys.com/prompts/) for their controller icons.

