package com.omegafrog.My.piano.app.lesson.entity.enums;

public class Instrument {
    private enum Piano {
        KEY_88("88키"),
        KEY_61("61키");
        final String description;

        Piano(String description) {
            this.description = description;
        }
    }

    private enum Guitar{
        ACOUSTIC("어쿠스틱"),
        ELECTRIC("일렉트릭"),
        BASE("베이스"),
        UKULELE("우쿨렐레");
        final String description;

        Guitar(String description) {
            this.description = description;
        }
    }

    private enum StringInstrument{
        VIOLIN("바이올린"),
        VIOLA("비올라"),
        CELLO("첼로"),
        BASE("베이스");

        final String description;

        StringInstrument(String description) {
            this.description = description;
        }
    }

    private enum WoodenWind{
        FLUTE("플루트"),
        PICCOLO("피콜로"),
        OBOE("오보에");

        final String description;

        WoodenWind(String description) {
            this.description = description;
        }
    }
}
