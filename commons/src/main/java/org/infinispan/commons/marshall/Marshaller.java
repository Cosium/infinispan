package org.infinispan.commons.marshall;

import java.io.IOException;

import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.io.ByteBuffer;

import net.jcip.annotations.ThreadSafe;

/**
 * A marshaller is a class that is able to marshall and unmarshall objects efficiently.
 * <p/>
 * This interface is used to marshall {@link org.infinispan.commands.ReplicableCommand}s, their parameters and their
 * response values, as well as any other arbitraty Object <--> byte[] conversions, such as those used in client/server
 * communications.
 * <p/>
 * A single instance of any implementation is shared by multiple threads, so implementations <i>need</i> to be threadsafe,
 * and preferably immutable.
 *
 * @author Manik Surtani
 * @version 4.1
 */
@ThreadSafe
public interface Marshaller {

   /**
    * Marshalls an object to a byte array.  The estimatedSize parameter is a hint that can be passed in to allow for
    * efficient sizing of the byte array before attempting to marshall the object.  The more accurate this estimate is,
    * the less likely byte[]s will need to be resized to hold the byte stream generated by marshalling the object.
    *
    * @param obj           object to convert to a byte array.  Must not be null.
    * @param estimatedSize an estimate of how large the resulting byte array may be
    * @return a byte array with the marshalled form of the object
    * @throws IOException if marshalling cannot complete due to some I/O error
    * @throws InterruptedException if the marshalling was interrupted. Clients should take this as a sign that
    * the marshaller is no longer available, maybe due to shutdown, and so no more unmarshalling should be attempted.
    */
   byte[] objectToByteBuffer(Object obj, int estimatedSize) throws IOException, InterruptedException;

   /**
    * Marshalls an object to a byte array.
    *
    * @param obj object to convert to a byte array.  Must not be null.
    * @return a byte array
    * @throws IOException if marshalling cannot complete due to some I/O error
    * @throws InterruptedException if the marshalling process was interrupted. Clients should take this as a sign that
    * the marshaller is no longer available, maybe due to shutdown, and so no more marshalling should be attempted.
    */
   byte[] objectToByteBuffer(Object obj) throws IOException, InterruptedException;

   /**
    * Unmarshalls an object from a byte array.
    *
    * @param buf byte array containing the binary representation of an object.  Must not be null.
    * @return an object
    * @throws IOException if unmarshalling cannot complete due to some I/O error
    * @throws ClassNotFoundException if the class of the object trying to unmarshall is unknown
    */
   Object objectFromByteBuffer(byte[] buf) throws IOException, ClassNotFoundException;

   /**
    * Unmarshalls an object from a specific portion of a byte array.
    *
    * @param buf    byte array containing the binary representation of an object.  Must not be null.
    * @param offset point in buffer to start reading
    * @param length number of bytes to consider
    * @return an object
    * @throws IOException if unmarshalling cannot complete due to some I/O error
    * @throws ClassNotFoundException if the class of the object trying to unmarshall is unknown
    */
   Object objectFromByteBuffer(byte[] buf, int offset, int length) throws IOException, ClassNotFoundException;

   /**
    * A method that returns an instance of {@link org.infinispan.commons.io.ByteBuffer}, which allows direct access to the byte
    * array with minimal array copying
    *
    * @param o object to marshall
    * @throws IOException if marshalling cannot complete due to some I/O error
    * @throws InterruptedException if the marshalling process was interrupted. Clients should take this as a sign that
    * the marshaller is no longer available, maybe due to shutdown, and so no more marshalling should be attempted.
    */
   ByteBuffer objectToBuffer(Object o) throws IOException, InterruptedException;

   /**
    * A method that checks whether the given object is marshallable as per the rules of this marshaller.
    *
    * @param o object to verify whether it's marshallable or not
    * @return true if the object is marshallable, otherwise false
    * @throws Exception if while checking whether the object was serializable or not, an exception arose
    */
   boolean isMarshallable(Object o) throws Exception;

   /**
    * Returns a marshalled payload size predictor for a particular type.
    * Accurate prediction of a type's serialized payload size helps avoid
    * unnecessary copying and speeds up application performance.
    *
    * @param o Object for which serialized predictor will be returned
    * @return an instance of {@link BufferSizePredictor}
    * @throws NullPointerException if o is null
    */
   BufferSizePredictor getBufferSizePredictor(Object o);

   /**
    * @return the {@link MediaType} associated with the content produced by the marshaller
    */
   MediaType mediaType();

}
