package org.duke.ui.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class UserInfo {
    public static final UserInfo DUKE = new UserInfo("Duke", Color.BLUE,
            Color.gray(0.9), Pos.CENTER_RIGHT);
    public static final UserInfo USER = new UserInfo("You", Color.GREEN,
            Color.gray(0.8), Pos.CENTER_LEFT);
    private final String name;
    private final Color nameColor;
    private final Color backgroundColor;
    private final Pos alignmentPosition;

    private UserInfo(String name, Color nameColor, Color backgroundColor, Pos alignmentPosition) {
        this.name = name;
        this.nameColor = nameColor;
        this.alignmentPosition = alignmentPosition;
        this.backgroundColor = backgroundColor;
    }

    public String getName() {
        return name;
    }

    public Color getNameColor() {
        return nameColor;
    }

    public Pos getAlignmentPosition() {
        return alignmentPosition;
    }

    public Background getBackground() {
        return new Background(new BackgroundFill(
                this.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY
        ));
    }
}
