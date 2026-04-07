package com.gbsw.snapy.domain.albums.entity;

public enum AlbumPhotoType {
    MORNING {
        @Override
        public boolean matches(int hour) {
            return hour >= 6 && hour < 12;
        }
    },
    LUNCH {
        @Override
        public boolean matches(int hour) {
            return hour >= 12 && hour < 18;
        }
    },
    DINNER {
        @Override
        public boolean matches(int hour) {
            return hour < 6 || hour >= 18;
        }
    },
    FREE_1 {
        @Override
        public boolean matches(int hour) {
            return true;
        }
    },
    FREE_2 {
        @Override
        public boolean matches(int hour) {
            return true;
        }
    };

    public abstract boolean matches(int hour);
}
