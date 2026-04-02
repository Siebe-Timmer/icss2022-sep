# Readme for ICSSTool
This file contains notes and issues for the ICSSTool.
For assignment instructions, see [ASSIGNMENT.md](ASSIGNMENT.md)
This tutorial is tested with Java version 17 (OpenJDK), IntelliJ and Maven. To enable instructors to assess your work you will need to keep your solution OpenJDK 13 compliant. Oracle or other distributions of the Java SDK/Runtime are not allowed.

## Running ICSSTool
ICSSTool is a `pom.xml` based, Maven-runnable application.
You can compile the application with the following command:

```mvn compile```

then run it with either

```mvn exec:java``` 
or
```mvn javfx:run```

Maven will automatically generate/update the parser from the supplied g4 file.

You can also run the application from an IDE, e.g. IntellIJ. To do so, import ICSSTool as Maven project. 
When you make changes to the .g4 file make sure you run `mvn generate-sources` prior to compiling. Most IDE's do not update the ANLTR parser automatically.

Since Java is modular, JavaFX is not bundled by default. Depending on your IDE you may need to download JavaFX and add it to your module path. See also: https://openjfx.io/openjfx-docs/

## Progress Tracking

### 4.1 Algemene Eisen (0 punten)
| ID | Omschrijving | Status |
|----|--------------|--------|
| AL01 | De code behoudt de packagestructuur van de aangeleverde startcode. Toegevoegde code bevindt zich in de relevante packages. | ✅ |
| AL02 | Alle code compileert en is te bouwen met Maven 3.6 of hoger, onder OpenJDK 13. | ✅ |
| AL03 | De code is goed geformatteerd, zo nodig voorzien van commentaar, correcte variabelenamen gebruikt, bevat geen onnodig ingewikkelde constructies en is zo onderhoudbaar mogelijk opgesteld. | ⏳ |
| AL04 | De docent heeft vastgesteld dat de compiler eigen werk is en dat je voldoet aan de beoordelingscriteria van APP-6. | ⏳ |

### 4.2 Parseren (40 punten)
| ID | Omschrijving | Prio | Punten | Status |
|----|--------------|------|--------|--------|
| PA00 | De parser dient zinvol gebruik te maken van **jouw** eigen implementatie van een stack generic voor `ASTNode` (`IHANStack<ASTNode>`) | Must | 0 | ✅ |
| PA01 | Implementeer een grammatica plus listener die AST's kan maken voor ICSS documenten die "eenvoudige opmaak" kan parseren. `level0.icss` kan worden geparseerd. `testParseLevel0()` slaagt. | Must | 10 | ✅ |
| PA02 | Breid je grammatica en listener uit zodat nu ook assignments van variabelen en het gebruik ervan geparseerd kunnen worden. `level1.icss` kan worden geparseerd. `testParseLevel1()` slaagt. | Must | 10 | ✅ |
| PA03 | Breid je grammatica en listener uit zodat je nu ook optellen en aftrekken en vermenigvuldigen kunt parseren, rekening houdend met de rekenregels. `level2.icss` kan worden geparseerd. `testParseLevel2()` slaagt. | Must | 10 | ✅ |
| PA04 | Breid je grammatica en listener uit zodat je if/else-statements aankunt. `level3.icss` kan worden geparseerd. `testParseLevel3()` slaagt. | Must | 10 | ✅ |
| PA05 | PA01 t/m PA04 leveren minimaal 30 punten op | Must | 0 | ✅ |

### 4.3 Checken (30 punten)
| ID | Omschrijving | Prio | Punten | Status |
|----|--------------|------|--------|--------|
| CH00 | Minimaal vier van onderstaande checks **moeten** zijn geïmplementeerd | Must | 0 | ✅ |
| CH01 | Controleer of er geen variabelen worden gebruikt die niet gedefinieerd zijn. | Should | 5 | ✅ |
| CH02 | Controleer of de operanden van de operaties plus en min van gelijk type zijn. Controleer dat bij vermenigvuldigen minimaal een operand een scalaire waarde is. | Should | 5 | ✅ |
| CH03 | Controleer of er geen kleuren worden gebruikt in operaties (plus, min en keer). | Should | 5 | ✅ |
| CH04 | Controleer of bij declaraties het type van de value klopt met de property. | Should | 5 | ✅ |
| CH05 | Controleer of de conditie bij een if-statement van het type boolean is (zowel bij een variabele-referentie als een boolean literal) | Should | 5 | ✅ |
| CH06 | Controleer of variabelen enkel binnen hun scope gebruikt worden | Must | 5 | ✅ |

### 4.4 Transformeren (20 punten)
| ID | Omschrijving | Prio | Punten | Status |
|----|--------------|------|--------|--------|
| TR01 | Evalueer expressies. Schrijf een transformatie in `Evaluator` die alle `Expression` knopen in de AST door een `Literal` knoop met de berekende waarde vervangt. | Must | 10 | ✅      |
| TR02 | Evalueer if/else expressies. Schrijf een transformatie in `Evaluator` die alle `IfClause`s uit de AST verwijdert volgens de conditie-logica. | Must | 10 | ✅      |

### 4.5 Genereren (10 punten)
| ID | Omschrijving | Prio | Punten | Status |
|----|--------------|------|--------|--------|
| GE01 | Implementeer de generator in `nl.han.ica.icss.generator.Generator` die de AST naar een CSS2-compliant string omzet. | Must | 5 | ✅ |
| GE02 | Zorg dat de CSS met twee spaties inspringing per scopeniveau gegenereerd wordt. | Must | 5 | ✅ |

### 4.6 Eigen Uitbreidingen (20 punten)
| Omschrijving | Punten | Status |
|--------------|--------|--------|
| TBD - Af te spreken met docent | Max 20 | ❌      |

**Huidige stand: 100/120 punten**

## Known issues
* Packaging works, but running the JAR standalone can be troublesome because of the JavaFX and ANLTR-runtime dependencies. You can uncomment the `maven-shade-plugin` in `pom.xml` to create a (huge) fat JAR. It removes module encapsulation which will trigger a warning.
* ICSSTool comes with tests to verify the AST based on sample input files. These are not true unit tests; they are included to help you verify your use of the AST.
