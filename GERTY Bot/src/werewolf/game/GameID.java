package werewolf.game;

public class GameID {
    private static String[][] ids = {
        { "And", "Andromeda" }, { "Ant", "Antlia " }, { "Aps", "Apus " }, { "Aqr", "Aquarius" }, { "Aql", "Aquila" },
        { "Ara", "Ara" }, { "Ari", "Aries" }, { "Aur", "Auriga" }, { "Boo", "Bootes" }, { "Cae", "Caelum" },
        { "Cam", "Camelopardalis" }, { "Cnc", "Cancer" }, { "CVn", "Canes Venatici" }, { "CMa", "Canis Major" },
        { "CMi", "Canis Minor" }, { "Cap", "Capricornus" }, { "Car", "Carina" }, { "Cas", "Cassiopeia" },
        { "Cen", "Centaurus" }, { "Cep", "Cepheus" }, { "Cet", "Cetus" }, { "Cha", "Chamaleon " },
        { "Cir", "Circinus" }, { "Col", "Columba" }, { "Com", "Coma Berenices" }, { "CrA", "Corona Australis" },
        { "CrB", "Corona" }, { "Crv", "Corvus" }, { "Crt", "Crater" }, { "Cru", "Crux" }, { "Cyg", "Cygnus" },
        { "Del", "Delphinus" }, { "Dor", "Dorado" }, { "Dra", "Draco" }, { "Equ", "Equuleus" }, { "Eri", "Eridanus" },
        { "For", "Fornax" }, { "Gem", "Gemini" }, { "Gru", "Grus" }, { "Her", "Hercules" }, { "Hor", "Horologium" },
        { "Hya", "Hydra" }, { "Hyi", "Hydrus" }, { "Ind", "Indus" }, { "Lac", "Lacerta" }, { "Leo", "Leo" },
        { "LMi", "Leo Minor" }, { "Lep", "Lepus" }, { "Lib", "Libra" }, { "Lup", "Lupus" }, { "Lyn", "Lynx" },
        { "Lyr", "Lyra" }, { "Men", "Mensa" }, { "Mic", "Microscopium" }, { "Mon", "Monoceros" }, { "Mus", "Musca" },
        { "Nor", "Norma" }, { "Oct", "Octans" }, { "Oph", "Ophiucus" }, { "Ori", "Orion" }, { "Pav", "Pavo" },
        { "Peg", "Pegasus" }, { "Per", "Perseus" }, { "Phe", "Phoenix" }, { "Pic", "Pictor" }, { "Psc", "Pisces" },
        { "PsA", "Pisces Austrinus" }, { "Pup", "Puppis" }, { "Pyx", "Pyxis" }, { "Ret", "Reticulum" },
        { "Sge", "Sagitta	 " }, { "Sgr", "Sagittarius" }, { "Sco", "Scorpius" }, { "Scl", "Sculptor" },
        { "Sct", "Scutum" }, { "Ser", "Serpens" }, { "Tau", "Taurus" }, { "Tel", "Telescopium" },
        { "Tri", "Triangulum" }, { "TrA", "Triangulum Australe" }, { "Tuc", "Tucana" }, { "UMa", "Ursa Major" },
        { "UMi", "Ursa Minor" }, { "Vel", "Vela" }, { "Vir", "Virgo" }, { "Vol", "Volans" }, { "Vul", "Vulpecula" }
    };

    public static String[] get(String threadId) {
        return ids[Math.abs(threadId.hashCode() % ids.length)];
    }
}
