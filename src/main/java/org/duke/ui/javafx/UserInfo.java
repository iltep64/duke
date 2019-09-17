package org.duke.ui.javafx;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

public class UserInfo {
    public static final UserInfo DUKE = new UserInfo("Duke", Color.BLUE,
            Color.gray(0.9), Pos.CENTER_RIGHT, "DaDuke.png");
    public static final UserInfo USER = new UserInfo("You", Color.GREEN,
            Color.gray(0.8), Pos.CENTER_LEFT, "DaUser.png");
    private final String name;
    private final Color nameColor;
    private final Color backgroundColor;
    private final Pos alignmentPosition;
    private final Image image;

    private UserInfo(String name, Color nameColor, Color backgroundColor,
                     Pos alignmentPosition, String imagePath) {
        this.name = name;
        this.nameColor = nameColor;
        this.alignmentPosition = alignmentPosition;
        this.backgroundColor = backgroundColor;
        this.image = new Image(DukeFx.class.getResourceAsStream(imagePath));
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

    public NodeOrientation getOrdering() {
        switch (alignmentPosition.getHpos()) {
        case RIGHT:
        default:
        case LEFT:
            return NodeOrientation.LEFT_TO_RIGHT;
        }
    }

    public Background getBackground() {
        return new Background(new BackgroundFill(
                this.backgroundColor, CornerRadii.EMPTY, Insets.EMPTY
        ));
    }

    public Image getImage() {
        return image;
    }
}
