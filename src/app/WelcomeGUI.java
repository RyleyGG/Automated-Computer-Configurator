import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.*;
import javafx.geometry.Pos;

public class WelcomeGUI extends VBox
{
    public WelcomeGUI(Scene scene)
    {
        this.prefWidthProperty().bind(scene.widthProperty());
        this.prefHeightProperty().bind(scene.heightProperty());
        this.spacingProperty().bind(scene.heightProperty().multiply(0.0075));
        this.setAlignment(Pos.BASELINE_CENTER);
        
        Text text = new Text("What would you like to do?");
        Button newBuildsButton = new Button("New Build Set");
        Button loadBuildsButton = new Button("Load Build Set (Currently does nothing)");
        
        //This button does nothing and is invisible to the user -- it exists to help position the other elements properly
        //Will be removed when its determined how to bind the text font size/position to the scene attributes properly
        Button bufferButton = new Button();
        bufferButton.setDisable(true);
        bufferButton.setVisible(false);
        bufferButton.prefHeightProperty().bind(scene.heightProperty().multiply(0.35));

        newBuildsButton.prefWidthProperty().bind(scene.widthProperty().multiply(0.95));
        newBuildsButton.setAlignment(Pos.BASELINE_CENTER);
        loadBuildsButton.prefWidthProperty().bind(scene.widthProperty().multiply(0.95));
        loadBuildsButton.setAlignment(Pos.BASELINE_CENTER);

        
        this.getChildren().addAll(bufferButton,text,newBuildsButton,loadBuildsButton);
    }
}

