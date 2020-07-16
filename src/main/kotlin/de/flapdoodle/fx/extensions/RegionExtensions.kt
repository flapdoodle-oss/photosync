package de.flapdoodle.fx.extensions

import javafx.scene.layout.Region

fun <T: Region> T.minWithFromPrefered(): T {
    this.minWidth = Region.USE_PREF_SIZE
    return this;
}