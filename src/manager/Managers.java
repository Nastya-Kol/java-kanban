package manager;

public class Managers {
    
    private Managers () {
    }
    
    public static TaskManager getDefaultManager(){
        return new InMemoryTaskManager();
    }
    
    public static HistoryManager getDefaultHistoryMemory (){
        return new InMemoryHistoryManager ();
    }
}
