package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Interpreter.ByteArrayWrapper;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.State;

/**
 * Created by tomaskohout on 11/21/15.
 */
public abstract class HeapItem extends ByteArrayWrapper {
    final int GC_STATE_SIZE = 1;



    public void setGCState(State state){
        byte b = 0;
        if (state == State.Live){
            b = 1;
        }

        byteArray[0] = b;
    }

    public State getGCState(){
        byte b = byteArray[0];
        return (b == 1) ? State.Live : State.Dead;
    }

    @Override
    public String toString() {
        return "GC: " + getGCState();
    }

}
