
package wormappsfx;

import annotationtoolfx.view.MainWindow;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import wormappsfx.Login;

/**
 *
 * @author MSHAO1
 * @lastModifiedBy jpiane
 */


public class WormAppsFX extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
     
        FXMLLoader loginloader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root;
	
        root = (Parent)loginloader.load();
        Stage newWindow = new Stage();

        Login login = loginloader.getController();
        newWindow.setTitle("Login");
        newWindow.setScene(new Scene(root, 400, 150));
        newWindow.showAndWait();

        if(!login.getResult()) {
            return;
        }

        
        FXMLLoader chooseloader = new FXMLLoader(getClass().getResource("ChooseApp.fxml"));
	
        root = (Parent)chooseloader.load();
	
        ChooseAppController chooseApp = chooseloader.getController();
        newWindow.setTitle("Choose Application");
        newWindow.setScene(new Scene(root, 280, 190));
        newWindow.showAndWait();
        
        ChooseAppOptions res = chooseApp.getResult();
        
        switch(res){
            case Cancel:
                return;
            case Viewer:
                
                FXMLLoader ld = new FXMLLoader(getClass().getResource("/wormviewerfx/WormViewerFXMain.fxml"));
                root = ld.load();

                Scene scene = new Scene(root);

                stage.setResizable(false);
                stage.setScene(scene);
                stage.setTitle("WormViewerFX");
                stage.show();
                break;
            case Annotations:
                FXMLLoader mwLoader = new FXMLLoader(getClass().getResource("/annotationtoolfx/view/MainWindow.fxml"));
		root = (Parent)mwLoader.load();
                MainWindow mw = mwLoader.getController();
                mw.setStage(stage);
		stage.setTitle("Worm Video");
		Scene mwScene = new Scene(root, 1550, 775);
		//mwScene.getStylesheets().add("/application/application.css");
		stage.setScene(mwScene);
	        
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	            public void handle(WindowEvent we) {
                   	mw.checkSave();
	            }
		});        
	        stage.show();
	     
                
                break;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
