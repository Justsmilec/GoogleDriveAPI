import com.google.api.client.json.JsonFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleDrive {

    /*
        This command-line app makes requests to the Drive API
     */

    private static final String APPLICATION_NAME = "GoogleDrive";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    // Directory to store user credentials for this application.
    private static final java.io.File CREDENTIALS_FOLDER = new java.io.File(System.getProperty("user.dir"), "credentials");
    private static final String CLIENT_SECRET_FILE_NAME = "client_secret.json";

    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);


    private static List<File> oFilesList;




    public static void main(String... args) throws IOException, GeneralSecurityException {
        System.out.println("CREDENTIALS_FOLDER: " + CREDENTIALS_FOLDER.getAbsolutePath());

        // 1: Create CREDENTIALS_FOLDER
        if (!CREDENTIALS_FOLDER.exists()) {
            CREDENTIALS_FOLDER.mkdirs();

            System.out.println("Created Folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
            System.out.println("Copy file " + CLIENT_SECRET_FILE_NAME + " into folder above.. and rerun this class!!");
            return;
        }

        // 2: Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // 3: Read client_secret.json file & create Credential object.
        Credential credential = getCredentials(HTTP_TRANSPORT);

        // 5: Create Google Drive Service.
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential) //
                .setApplicationName(APPLICATION_NAME).build();

        // Print the names and IDs for up to 10 files.
//        FileList result = service.files().list().setPageSize(10).setFields("nextPageToken, files(id, name)").execute();

        // List files inside a directory
//        FileList result = service.files().list()
//                //.setQ("'root' in parents and trashed = false") this gets all files inside root, if you want to change root to a folder then set folderId instead of root
//                .setQ("'1wx-82gPxmwsbcqk1ymBk3NkNW7r5XkOb' in parents and trashed = false")
//                .setFields("nextPageToken, files(id, name, mimeType)")
//                .execute();
        oFilesList = new ArrayList<>();
        retrieveAllFilesOnDirectory(service,"1IMTJ_CvA0LBrW4zKrsu6Ho28wEMsmqgx",oFilesList);
    }

    public static void retrieveAllFilesOnDirectory(Drive service,String folderId,List<File> list) throws IOException {

        FileList result = service.files().list()
                //.setQ("'root' in parents and trashed = false") this gets all files inside root, if you want to change root to a folder then set folderId instead of root
                .setQ("'" + folderId + "' in parents and trashed = false")
                .setFields("nextPageToken, files(*)")
                .execute();

        List<File> files = result.getFiles();
        if (files == null ) {
            System.out.println("No files found.");
            return;
        }
            for (File file : files) {
                if(file.getCapabilities().getCanAddChildren() == false){
                    list.add(file);
                    System.out.printf("%s (%s)\n", file.getName(), file.getId());
                }
                retrieveAllFilesOnDirectory(service,file.getId(),list);

        }

    }


    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {

        java.io.File clientSecretFilePath = new java.io.File(CREDENTIALS_FOLDER, CLIENT_SECRET_FILE_NAME);

        if (!clientSecretFilePath.exists()) {
            throw new FileNotFoundException("Please copy " + CLIENT_SECRET_FILE_NAME //
                    + " to folder: " + CREDENTIALS_FOLDER.getAbsolutePath());
        }
        // Load client secrets.
        InputStream in = new FileInputStream(clientSecretFilePath);

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                clientSecrets, SCOPES).setDataStoreFactory(new FileDataStoreFactory(CREDENTIALS_FOLDER))
                .setAccessType("offline").build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

}
