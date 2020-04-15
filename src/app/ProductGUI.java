//**********************************************************
// Class: ProductGUI
// Author: Ryley G.
// Date Modified: April 13, 2020
//
// Purpose: Conveys the progress the application has made in regards to grabbing product information
//
//
//************************************************************

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.text.*;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.collections.FXCollections;
import javafx.scene.control.TextFormatter;
import javafx.util.converter.IntegerStringConverter;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;


class ProductGUI extends VBox
{
    public ProductGUI(Scene scene, Configurator configurator)
    {
        GenericApplication[] appList = configurator.getAppList().toArray(new GenericApplication[0]);

        configurator.cacheHardwareData();
        configurator.lookupGPU("GeForce GTX 950");

    }
}