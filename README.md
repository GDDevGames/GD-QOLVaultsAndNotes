# Projektnamn
GDS-QOLVaultsAndNotes

## Deltagare
* Theodor Hedhman Nilsson - Projekt ledare och managerare, ansvarig för kodning och Figma prototypen samt vissa gränssnitt.
* Daniel Laxén - Stort sätt ansvarig för kodning och problemlösning tillsammans med att hjälpa fördjupa gruppens förståelse om Minecraft moddning.
* Alvin Hansson - 3D modellering och texturer, ansvarig för kodgranskning, kommentarer och ljuddesign samt delvis bidra med förståelse för Minecraft moddning tillsammans med Daniel.

## Beskrivning
GDS-QOLVaultsAndNotes är ett modd som skapades för att tillåta Quality of Life förändringar i Minecraft. Genom att använda sig av moddingsramverket NeoForge har vi skapat en modd som kan köras på en NeoForge Minecraft-klient.

Anledningen till att vi skapade detta projekt är att det mötte flest betygskrav av våra idéer, samt att det kändes som enklast att utföra tack vare mängden guider som fanns på internet. 
De föremål och block som vi skapade var till för att uppfylla funktioner som skulle kunna användas av olika spelare med olika syften.

Vi har lagt till fem föremål i spelet:
Anslagstavla (bulletin board)
Kassaskåp (safe)
Lås (lock)
Nyckel (key)
Nyckelkort (keycard)

Recepten för föremålen låses upp genom att spela spelet och att få tag på deras respektiva ingredienser.

### Anslagstavla | Bulletin Board
Anslagstavlan är ett väggmonterat block som låter dig skriva och spara upp mot åtta "post-it-lappar".
På din post-itlapp kan du skriva en titel med max. 12 bokstäver.

Huvuddelen låter dig skriva, färga och därefter nåla fast din post-it lapp på anslagstavlan. Du kan även klicka på existerande post-it lappar för att visa eller ändra dem.

Anslagstavlan kräver papper och bläck för att kunna skapa lappar. Papper och bläck laddas in i anslagstavlan genom att högerklicka anslagstavlan medans du håller ett av föremålen i handen.

### Kassaskåp | Safe
Kassaskåp förvarar 2 rader av föremål, som motsvarar 18 föremål (2x9). Till skillnad från vanliga kistor är de är riktigt hårda och tar lång tid att ta sönder.

Vanligtvis funkar de som vanliga kistor men kan låsas med eller utan en kod. 
För att säkra det, kan du högerklicka på det med ett lås. Det hindrar andra spelare än dig från att låsa upp det. Om du vill ge en kod till skåpet så att andra också kan låsa upp det om de har rätt kod, kan du använda dig av en nyckel eller ett nyckelkort.

* Genom att högerklicka på ett låst kassaskåp med en nyckel kan man sätta en seriekod, som låses upp med en kombination av 18 på- eller avslagna segment. 
* Genom att högerklicka på ett låst kassaskåp med ett nyckelkort kan man sätta en pinkod, som låses upp genom att trycka i rätt pinkod och sedan trycka knappen för att bekräfta.

Att ge en kod till sitt kassaskåp fungerar endast om du har döpt din nyckel eller nyckelkort i ett städ. 

För nycklar bör namnet endast bestå av **punkter (.) eller bindestreck (-).** 
Om namnet du ger nyckeln är kortare än 18 tecken, kommer resten att fyllas med bindestreck.
Punkter indikerar påslagna segment, bindestreck indikerar avslagna segment.

För nyckelkort bör namnet endast bestå av **siffror (0-9).**
Namnet av ditt nyckelkort utser pinkoden som kassaskåpet får.

### Kassavalv | Vault
Kassavalv är ett större block som bildas när du placerar 8 kassaskåp i en 2x2x2 kub formering. Det kan innehålla upp till 72 föremål, som är åtta rader föremål man kan skrolla genom. Det har likadana möjligheter av säkerhet som kassaskåp med hjälp av lås och nyckel eller nyckelkort. 

Det är det hårdaste blocket i Minecraft och tar ungefär **2 minuter att förstöra med en netherithacka** som har förtrollningen effektivitet 5.
Utan förtrollningen effektivitet 5 beräknas det ta **8 minuter att förstöra med en netherithacka.**
Med en vanlig järnhacka tar det enligt beräkning runt 16 minuter att förstöra kassavalvet.

### Nyckelkort | Keycard
Nyckelkort kan utöver sin säkerhetsfunktion också sätta på eller av netherportaler genom att **högerklicka på obsidianramen av en netherportal.**

Om du högerklickar på en kolv eller klibbig kolv i Minecraft kommer det att tvingas den att **aktiveras och deaktiveras, vilket överskrider närliggande rödstensignaler.**

### Nyckel | Key
Nyckeln kan utöver sin säkerhetsfunktion även användas för att **öppna och stänga järndörrar,** samt att **göra om kistfällor till kistor.**


## Kom igång
### Förutsättningar
* Java 21
* Minecraft 1.21.11
* Neoforge 21.11+

### Installation (om möjligt)
Vi rekommenderar att du installerar moddet via Curseforge appen.
1. Ladda ner Curseforge appen.
2. Skapa ett modpack med Neoforge version 1.21.11.
3. Sök efter vårt modd och installera det.

*Om du inte vill använda Curseforge kan du manuellt installera .jar filen.*
1. Ladda ner .jar filen antingen från Github eller Curseforge.
2. Installera filen i din `.minecraft/mods` mapp inuti `%AppData%/Roaming`.
3. Öppna Minecraft launchern och välj Neoforge 1.21.11.

### Köra programmet
Starta Minecraft 1.21.11 med Neoforge installerat. Neoforge kommer automatiskt ladda moddet.

## Externt material

Vår github – https://github.com/GDDevGames/GDS-QOLVaultsAndNotes

Ladda ner curseforge – https://www.curseforge.com/download/app

Köp minecraft om du inte äger spelet – https://www.minecraft.net/sv-se/store/minecraft-java-bedrock-edition-pc?tabs=%7B"details"%3A0%7D

Om du vill titta på moddet upplagt på curseforge, men du laddar ner moddet i curseforge “appen” – https://www.curseforge.com/minecraft/mc-mods/gds-qolvaultsandnotes

Länk till Trellon för att gå med som medlem – https://trello.com/invite/b/697897deb22b5920970333e7/ATTIf4b056a5e7a91fe66413f041c4bf74984A482363/minecraft-mod

Länk till Trello för att endast kunna se trellon – https://trello.com/b/m6wsAPnK

Länk till Figma prototypen – https://www.figma.com/design/ghfD8fSDsHeuvbI2YRuVaX/Minecraft-mod?node-id=0-1&t=yFb5TsZ2hbVHOPFm-1

Länk till dokumentationen som har blivit gjord under projektets gång – https://docs.google.com/document/d/1EA0hVou7y3uEiBCShiZJY-tVdepHkfscDSW-DFanOSg/edit?usp=sharing

Länk till skulle-vara-planering till projektet, men användes inte så mycket, i historiken visar den den planering vi gjorde från början, upplägg för alla veckor framöver – https://docs.google.com/document/d/119IYSFb-zVCZXMHLKcOvPVTwOcSC705B_ULSkwjXOMQ/edit?usp=sharing

Länk till de sprint reviews vi har skrivit, det gjordes ingen på de sista veckorna av projektet, men den hade i princip bara sagt “nästan klar, vi ska buggfixa” – https://docs.google.com/document/d/1wEfQJFo2fer1lQH_K_ibOC30JQq8J1oUrwSg1Mnh77Q/edit?usp=sharing

Länk till kalkylarket vi skapade för att lägga ut på bordet vilka betygskrav olika projektidéer uppnådde, därmed valet av detta projektet – https://docs.google.com/spreadsheets/d/1ZxfthoGPZMdIQsVtpd4LAZ7lO7vY1rZIDGgfLNxb4h8/edit?usp=sharing
