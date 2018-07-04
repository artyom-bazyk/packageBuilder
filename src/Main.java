import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;

public class Main {
    public static void listEntries( SVNRepository repository, String path ) throws SVNException {
        Collection entries = repository.getDir( path, -1 , null , (Collection) null );
        Iterator iterator = entries.iterator( );
        while ( iterator.hasNext( ) ) {
            SVNDirEntry entry = ( SVNDirEntry ) iterator.next( );
            System.out.println( "/" + (path.equals( "" ) ? "" : path + "/" ) + entry.getName( ) + 
                               " ( author: '" + entry.getAuthor( ) + "'; revision: " + entry.getRevision( ) + 
                               "; date: " + entry.getDate( ) + ")" );
            if ( entry.getKind() == SVNNodeKind.DIR ) {
                listEntries( repository, ( path.equals( "" ) ) ? entry.getName( ) : path + "/" + entry.getName( ) );
            }
        }
    }
    
    public static void printChanges(SVNLogEntry logEntry, SVNRepository repository) throws Exception{
            System.out.println( "---------------------------------------------" );
            System.out.println ("revision: " + logEntry.getRevision( ) );

            if ( logEntry.getChangedPaths( ).size( ) > 0 ) {
                System.out.println( );
                System.out.println( "changed paths:" );
                Set changedPathsSet = logEntry.getChangedPaths( ).keySet( );

                for ( Iterator changedPaths = changedPathsSet.iterator( ); changedPaths.hasNext( ); ) {
                    SVNLogEntryPath entryPath = ( SVNLogEntryPath ) logEntry.getChangedPaths( ).get( changedPaths.next( ) );
                    
            		File packageFolder = new File("E:\\test\\");
                    deleteDirectory(packageFolder);
                    packageFolder.mkdir();
                    
                    if(entryPath.getType() != SVNLogEntryPath.TYPE_DELETED){
                        writeFile(repository, entryPath.getPath(), packageFolder.getPath());  
                        writeFile(repository, entryPath.getPath() + "-meta.xml", packageFolder.getPath());                    	
                    }
                    System.out.println( " "
                            + entryPath.getType( )
                            + " "
                            + entryPath.getPath( ));
                }
            }
    }
    
    public static Integer getBugId(String message){
    	Pattern pattern = Pattern.compile("[0-9]+");
    	Matcher matcher = pattern.matcher(message);
    	matcher.find();
    	Integer bugId = null;
    	try{
        	bugId = Integer.parseInt(matcher.group());    		
    	} catch(Exception e){
    	}
    	return bugId;
    }
        
    public static void writeFile(SVNRepository repository, String filePath, String packagePath) throws Exception{
    	SVNNodeKind nodeKind = repository.checkPath( filePath , -1 );
    	if(nodeKind == SVNNodeKind.NONE ){
    		return;
    	}
    	
        SVNProperties fileProperties = new SVNProperties();
        ByteArrayOutputStream baos = new ByteArrayOutputStream( );
        repository.getFile( filePath , -1 , fileProperties , baos );
        
        File file = new File(filePath);
        String dir = file.getParent().substring(file.getParent().lastIndexOf(File.separator) + 1);
        String path = packagePath + "\\" + dir + "\\" +file.getName();
        file = new File(path);
        file.getParentFile().mkdir();
        FileOutputStream out = new FileOutputStream(file, false);
        try {
            baos.writeTo( out );
        } catch ( IOException ioe ) {
            ioe.printStackTrace( );
        }
    }
    
	public static void deleteDirectory(File directory) {
		if (directory.exists()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					if (files[i].isDirectory()) {
						deleteDirectory(files[i]);
					} else {
						files[i].delete();
					}
				}
			}
		}
		directory.delete();
	}    
    
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		try {
			String username = "";
			String password = "";
			String svnURL = "";

			Integer bugId = 2444; // set bug Id from svn

			ISVNAuthenticationManager authManager = new BasicAuthenticationManager( username , password );
			SVNRepositoryFactoryImpl.setup();
			SVNURL url = SVNURL.parseURIDecoded(svnURL);
			SVNRepository repository = SVNRepositoryFactory.create(url, null);
			
			repository.setAuthenticationManager(authManager);
			System.out.println( "Repository Root: " + repository.getRepositoryRoot( true ) );
			
			Collection<SVNLogEntry> logEntries = repository.log( new String[] { "" } , null , 4000 , repository.getLatestRevision() , true , true );
	        for (SVNLogEntry logEntry : logEntries) {
	        	Integer entrylogId = getBugId(logEntry.getMessage());
	        	if(entrylogId != null && entrylogId.equals(bugId)){
		        	System.out.println(entrylogId);
		        	printChanges(logEntry, repository);	        		
	        	}
	        }
          
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
