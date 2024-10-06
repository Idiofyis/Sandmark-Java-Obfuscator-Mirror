package sandmark;

public class Constants {

    public static final boolean DEBUG = false;

   /**
    * Return the major version number.
    */
   public static int majorVersion() {
      return 3;
   }

   /**
    * Return the minor version number.
    */
   public static int minorVersion() {
      return 4;
   }

   /**
    * Return the sub-minor version number.
    */
   public static int subMinorVersion() {
      return 0;
   }

   /**
    * Return the nickname.
    * Past names:
    * <pre>
    *     3.3: Xavier
    *     3.4: Mystique
    * </pre>
    * Future names:
    * <pre>
    *    Archangel Beast Cyclops Emma Frost Iceman
    *    Jean Grey Juggernaut Magneto Nightcrawler
    *    Sentinels Storm Wolverine Cablee Changeling
    *    Banshee Cannonball Colossus Dazzler Psylocke
    *    Bishop Forge Thunderbird I Chamber Havok Jubilee
    *    Emma Frost Longshot Gambit Maggott Magneto Lifeguard
    *    Marrow Polaris Shadowcat Northstar Sunfire Phoenix
    *    Rogue Stacy X Storm Thunderbird III Wolverine
    * </pre>
    * See also <a href="http://www.marveldirectory.com/teams/xmen.htm"> Marvel Directory </a>
    */
   public static String nickName() {
      return "Mystique";
   }

   /**
    * Return the version number as a string.
    */
   public static String versionString() {
       return
          sandmark.Constants.majorVersion() +
          "." +
          sandmark.Constants.minorVersion() +
          "." +
          sandmark.Constants.subMinorVersion();
   }

   /**
    * Return the version number as a string, including the nickname.
    */
   public static String longVersionString() {
       return
          sandmark.Constants.versionString() +
          " (" + sandmark.Constants.nickName() + ")";
   }

    public static void main ( String args[] ) {
        if ( args.length > 0 ) {
            if ( args[0].equals( "majorversion" ) ) {
                System.out.print( majorVersion() );
            } else if ( args[0].equals( "minorversion" )) {
                System.out.print( minorVersion() );
            } else if ( args[0].equals( "subminorversion" ) ) {
                System.out.print( subMinorVersion() );
            } else if ( args[0].equals( "version" ) ) {
                System.out.print( versionString() );
            } else if ( args[0].equals( "nickname" ) ) {
                System.out.print( nickName() );
            } else if ( args[0].equals( "longversion" ) ) {
                System.out.print( longVersionString() );
            }
        }
    }

}

