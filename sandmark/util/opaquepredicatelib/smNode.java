package sandmark.util.opaquepredicatelib;

public class smNode
{
   public smNode prev,next;
   public static smNode g=null,h=null;
   static boolean DEBUG=false;
   public int value;
   static int cnt=0;

   public smNode()
   {
      if (DEBUG)
         System.out.println("<INIT>");
      this.prev=this.next=this;
      this.value=cnt++;
      if(DEBUG)
         System.out.println(" Created smNode with value set to " + this.value);
   }

   public smNode Move(int i)
   {
      if (DEBUG)
         System.out.println("Move");
      return this.selectNode(i);
   }

   public smNode Insert(int i,int j,smNode p)
   {
      if (DEBUG)
         System.out.println("Insert");
      if(p==null)
         {
            p=new smNode();
            // p.prev=p;
            //p.next=p;
            return p;
         }
      else
         {
            smNode r=p.selectNode(i);
            if(DEBUG)
               System.out.println("Insert fn: Selectnode returned " + r.value);
            return r.addNode(j);
         }
   }

   public java.util.TreeSet reachableNodes()
   {
      return reachable(new java.util.TreeSet());
   }

   public java.util.TreeSet reachable(java.util.TreeSet reached)
   {

      /*	java.util.Stack s= new java.util.Stack();
         s.push((Object)this);
         s.push
         while(!s.empty() && !reached.contains((Object)this) )
         {
	    
	    
         }*/



      //if(!reached.contains((Node)this ))
      //{
      if(DEBUG)
         System.out.println("Adding " + this.value + " to the reachable set");
      if(reached.add(this)!=false)
         {
            //if(this.prev!=this)
            this.prev.reachable(reached);
            //else if(this.next !=this)
            this.next.reachable(reached);
         }
      //}
      return reached;
   }
    
   public void dodfs(smNode p,java.util.HashSet hs)
   {
      if(DEBUG)
         System.out.println("Adding Node "+p.value + " to the Hashset");

      hs.add((Object)p);

      if(hs.contains(p.prev)==false)
         {
            dodfs(p.prev,hs);
         }
      else if(hs.contains(p.next)==false)
         {
            dodfs(p.next,hs);
         }
   }

   public void createCut(smNode p, int wheretocut, java.util.HashSet hs)
   {
      if(DEBUG)
         System.out.println("Adding smNode "+p.value + " to the Hashset");

      if(hs.add(p)==false)
         return;

      /*
        if(hs.size()==wheretocut)
        {
        if( hs.contains(p.prev)==true && p.prev !=p)
        {
        if(DEBUG)
        System.out.println("Found Cut and prev pointer points to something already in HashSet");
        p.prev=p;
        return;
        }

        else if (hs.contains(p.next)==true && p.prev !=p)
        {
        if(DEBUG)
        System.out.println("Found Cut and next pointer points to something already in HashSet");
        p.prev=p;
        return;
        }
	   
        }*/


      if(hs.contains(p.prev)==false)
         {
            createCut(p.prev,wheretocut,hs);
         }
      else if(hs.contains(p.next)==false)
         {
            createCut(p.next,wheretocut,hs);
         }

   }

   public void reachableNodes(java.util.ArrayList al)
   {
      if(!al.contains(this))
         {
            if(DEBUG)
               System.out.println("Node " +this.value + " added");
            al.add(this);
         }
      else if(!al.contains(this.prev))
         {
            if(DEBUG)
               System.out.println("Node " +this.prev.value + " added");
            al.add(this.prev);
         }
      else if(!al.contains(this.next))
         {
            if(DEBUG)
               System.out.println("Node " +this.next.value + " added");
            al.add(this.next);
         }
      else
         return;
      this.prev.reachableNodes(al);
      this.next.reachableNodes(al);
   }

   public smNode Split(int index,smNode p)
   {
      if(DEBUG)
         System.out.println("p.value "+p.value+" p.prev" + p.prev.value + " p.next "+p.next.value );

      smNode q =p.selectNode(index);
      if(DEBUG)
         System.out.println("q.value "+q.value+" q.prev " + q.prev.value + " q.next "+q.next.value );
      java.util.ArrayList al1 = new java.util.ArrayList();
      java.util.ArrayList al2 = new java.util.ArrayList();
      p.reachableNodes(al1);
      q.reachableNodes(al2);
      /*TreeSet a = p.reachableNodes();
        TreeSet b= q.reachableNodes();
        if( a.equals(b) ==false)
        {
        a.removeAll(b);
        TreeSet diff= new TreeSet(a);
	    
        a=p.reachableNodes();
        a.retainAll(b);
        p.splitGraph(diff,b);
        return q;
        }
        else*/

      {	 
         java.util.HashSet hs = new java.util.HashSet();    
         int cut= al1.size()/2;
         createCut(p,cut,hs);
         java.util.Iterator i = hs.iterator();
         while(i.hasNext())
            {
               smNode temp=(smNode)i.next();
	     
               if(temp.prev == p)
                  {
                     if(DEBUG)
                        System.out.println("Node "+ temp.value+ " prev pointer points to "+ p.value);
                     temp.prev=temp;
                  }
               else if (temp.next==p)
                  {
                     if(DEBUG)
                        System.out.println("Node "+ temp.value+ " next pointer points to "+ p.value);
                     temp.next=temp;
                  }
            }	   

         return q;
      }
	
   }

   
   public void splitGraph(java.util.TreeSet a,java.util.TreeSet b)
   {
      java.util.Iterator iter=a.iterator();
      smNode temp;
      while(iter.hasNext())
         {
            temp=(smNode)iter.next();
            if(b.contains( (smNode)(temp.prev) ))
               temp.prev=temp;
            else if (b.contains( (smNode)(temp.next) ))
               temp.next=temp;
         }
	
   }

   public smNode addNode(int index)
   {
      if(index==1)
         {
            smNode p=new smNode();
            p.prev=this.prev;
            if(DEBUG)
               System.out.println("addNode fn : " +this.value);
            return this.prev=p;
         }
      else if (index==2)
         {
            smNode p=new smNode();
            p.next=this.prev;
            return this.prev=p;
         }
	
      return null;
   }
    
   public smNode selectNode4b(int n)
   {
      return null;
   }
   public smNode selectNode(int index)
   {
      if(index==1)
         {
            if(DEBUG)
               System.out.println("Node Number: "+ this.value+ " returned");
            return this;		}
      else if(index==2)
         {
            if(DEBUG)
               System.out.println("Node Number: "+ this.prev.value+ " returned");
            return this.prev;
         }
      else if(index==3)
         {
            if(DEBUG)
               System.out.println("Node Number: "+ this.prev.next.value+ " returned");
            return this.prev.next;
         }
      else if(index==4)
         {
            return (index<=0)?this:this.prev.selectNode4b(index-1);
         }
	
      return null;
   }
    
   public void Merge(smNode p,smNode q)
   {
      if(q.prev!=p)
         q.prev=p;
   }
   public void Link(int i,int j,smNode p)
   {
      smNode a= p.selectNode(i);
      smNode b= p.selectNode(j);
      if(b.prev==b)
         b.prev=a;
   }
}

