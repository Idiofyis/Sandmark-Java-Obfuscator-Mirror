package sandmark.wizard.decision.random;


/**
 * Strategy that randomly chooses sandmark algorithms to run for each step.
 *
 */
public class Random extends sandmark.wizard.decision.priority.MaxPriorityStrategy {
   public boolean step() throws Exception {
      sandmark.wizard.decision.priority.MaxPriorityStrategy.ChoiceWrapper cw =
         (sandmark.wizard.decision.priority.MaxPriorityStrategy.ChoiceWrapper)
         mChoicePQ.get(0);
      float delta = sandmark.wizard.decision.priority.MaxPriorityStrategy.DELTA;
      if(cw.value < delta)
         return false;
      
      int choiceCount = mModel.getChoiceCount();
      while(true) {
         int choice = sandmark.util.Random.getRandom().nextInt() % choiceCount;
         if(choice < 0)
            choice += choiceCount;
         cw = (sandmark.wizard.decision.priority.MaxPriorityStrategy.ChoiceWrapper)
            mChoicePQ.get(choice);
         if(cw.value < delta)
            continue;
         mRunner.run(cw);
         return true;
      }
   }
}
