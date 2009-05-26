/*
 ** Authored by Timothy Gerard Endres
 ** <mailto:time@gjt.org>  
 ** 
 ** This work has been placed into the public domain.
 ** You may use this work in any way and for any purpose you wish.
 **
 ** THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND,
 ** NOT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR
 ** OF THIS SOFTWARE, ASSUMES _NO_ RESPONSIBILITY FOR ANY
 ** CONSEQUENCE RESULTING FROM THE USE, MODIFICATION, OR
 ** REDISTRIBUTION OF THIS SOFTWARE. 
 ** 
 */

package com.myJava.file.archive.tar;

import java.io.IOException;

/**
 * This exception is used to indicate that there is a problem
 * with a TAR archive header
 * 
 * <BR>
 * <BR>CAUTION :
 * <BR>This file has been integrated into Areca.
 * <BR>It is has also possibly been adapted to meet Areca's needs. If such modifications has been made, they are described above.
 * <BR>Thanks to the authors for their work.
 *.
 */

public class
InvalidHeaderException extends IOException
{
    
    public
    InvalidHeaderException()
    {
        super();
    }
    
    public
    InvalidHeaderException( String msg )
    {
        super( msg );
    }
    
}
