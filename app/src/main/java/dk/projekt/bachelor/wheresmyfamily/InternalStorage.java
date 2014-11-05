package dk.projekt.bachelor.wheresmyfamily;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by Tommy on 29-09-2014.
 */
public final class InternalStorage
{
    public static void writeObject(Context context, String fileName, Object object) throws IOException
    {
        FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(object);
        oos.close();
        fos.close();
    }

    public static Object readObject(Context context, String fileName) throws IOException, ClassNotFoundException
    {
        FileInputStream fis = context.openFileInput(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        // String[] children = (String[]) ois.readObject();
        Object object = ois.readObject();
        return object;
    }
}
