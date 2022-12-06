import java.io.*;
import java.nio.channels.ScatteringByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class preprocessing {
    public static void main(String[] args) {
        initialize();

        // preprocessing step 1
        /*
        //read_player_from_schedule();
        //read_player_from_savant();
        */

        // record linkage
        /*
        read_files_for_record_linkage();
        record_linkage();
        */

        read_record_linkage();

        // team schedule
        //generate_team_schedule_result();

        // player overall
        //generate_player_overall_performance();
        //generate_player_savant_performance();
        generate_player_play_for_match();

    }




    private static HashMap<String, String[]> player_info_from_schedule;
    private static HashMap<String, String[]> player_info_from_schedule_with_full_name;
    private static HashMap<String, String[]> player_info_from_savant;

    private static boolean with_full_name = false;



    private static void initialize(){
        player_info_from_schedule = new HashMap<String, String[]>();
        player_info_from_savant = new HashMap<String, String[]>();
        player_info_from_schedule_with_full_name = new HashMap<String, String[]>();
    }

    private static void read_player_from_schedule(){
        File directory = new File("Schedule");
        try{
            int reading_counter = 0;
            for(File sub_directory: directory.listFiles()){
                if(sub_directory.isDirectory()){
                    String game_general_info = sub_directory.getName();
                    //System.out.println("Schedule/"+game_general_info);

                    if(game_general_info.contains("All-Stars") || game_general_info.contains("-1")){
                        continue;
                    }

                    String game_team_info = game_general_info.substring(11, game_general_info.length());
                    String[] game_team_name = game_team_info.split(" VS. ");

                    String away_team = game_team_name[0];
                    String home_team = game_team_name[1];
                    //System.out.println(away_team+" --- "+home_team);

                    String[] team_pos = {"away", "home"};
                    String[] player_pos = {"batter", "pitcher"};

                    for(int t_pos=0; t_pos<2; t_pos++){
                        for(int p_pos=0; p_pos<2; p_pos++){
                            FileInputStream fis = new FileInputStream("Schedule/"+game_general_info+"/"+team_pos[t_pos]+"_"+player_pos[p_pos]+".csv");
                            Scanner sc = new Scanner(fis);
                            String this_team = game_team_name[t_pos];

                            sc.nextLine();
                            for(;sc.hasNextLine();){
                                String str = sc.nextLine();
                                String[] spt = str.split(",");
                                if(spt[0].length() > 0){
                                    String url_id = spt[0].split("/")[4];
                                    String[] player_info = new String[4];
                                    player_info[0] = url_id;
                                    player_info[1] = spt[0];
                                    player_info[2] = spt[1].replace("1-","");
                                    player_info[3] = this_team;
                                    //System.out.println(player_info[0]+", "+player_info[1]+", "+player_info[2]+", "+player_info[3]);

                                    if(!player_info_from_schedule.containsKey(url_id)){
                                        player_info_from_schedule.put(url_id, player_info);
                                    }
                                    else{
                                        String prob_prev_team = player_info_from_schedule.get(url_id)[3];
                                        if(!prob_prev_team.equals(this_team)){
                                            System.out.println("Player transfer: "+spt[1]+"("+prob_prev_team+" => "+this_team+")");
                                            player_info_from_schedule.put(url_id, player_info);
                                        }
                                    }
                                }
                            }
                            sc.close();
                            fis.close();
                        }
                    }



                    reading_counter++;
                    if(reading_counter%100==0){
                        //System.out.println("Reading source file... ("+reading_counter+"/622)");
                    }
                }
            }


            PrintWriter pw = new PrintWriter("player_info_from_schedule.csv");
            pw.println("Id,Url,PlayerName,Team");
            pw.flush();
            for(Object obj: player_info_from_schedule.keySet()){
                String[] this_player_info = player_info_from_schedule.get(obj);
                for(int traverse_info=0; traverse_info<this_player_info.length; traverse_info++){
                    pw.print(this_player_info[traverse_info]);
                    pw.flush();

                    if(traverse_info<this_player_info.length-1){
                        pw.print(",");
                        pw.flush();
                    }
                }
                pw.println("");
                pw.flush();
            }
            pw.flush();
            pw.close();

            System.out.println("Total Number of Players from MLB website: "+player_info_from_schedule.size());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void read_player_from_savant(){
        File directory = new File("Data/player_individual");
        try{
            int reading_counter = 0;
            for(File sub_directory: directory.listFiles()){
                if(sub_directory.isDirectory()){
                    String player_name_abbr = sub_directory.getName();
                    String[] spt = player_name_abbr.split("_");
                    String player_name = player_name_abbr;

                    if(spt.length == 2){
                        player_name = spt[1]+" "+spt[0];
                    }
                    else if(spt.length == 3){
                        player_name = spt[2]+" "+spt[0]+" "+spt[1];
                    }

                    FileInputStream fis = new FileInputStream(sub_directory+"/"+"Splits_MLB_Monthly_Splits.csv");
                    Scanner sc = new Scanner(fis);
                    String this_team = "";
                    sc.nextLine();
                    for(;sc.hasNextLine();){
                        String str = sc.nextLine();
                        String[] sptt = str.split(",");
                        if(!sptt[0].equals(this_team)){
                            this_team = sptt[0];
                        }
                    }
                    sc.close();
                    fis.close();

                    String[] player_info = new String[3];
                    player_info[0] = player_name_abbr;
                    player_info[1] = player_name;
                    player_info[2] = this_team;
                    //System.out.println(player_info[0]+", "+player_info[1]+", "+player_info[2]);

                    player_info_from_savant.put(player_info_from_savant.size()+"", player_info);
                }


                reading_counter++;
                if(reading_counter%50==0){
                    //System.out.println("Reading source file... ("+reading_counter+"/174)");
                }
            }


            PrintWriter pw = new PrintWriter("player_info_from_savant.csv");
            pw.println("Id,Uri,PlayerName,Team");
            pw.flush();
            for(Object obj: player_info_from_savant.keySet()){
                String[] this_player_info = player_info_from_savant.get(obj);
                pw.print(obj+",");
                pw.flush();
                for(int traverse_info=0; traverse_info<this_player_info.length; traverse_info++){
                    pw.print(this_player_info[traverse_info]);
                    pw.flush();

                    if(traverse_info<this_player_info.length-1){
                        pw.print(",");
                        pw.flush();
                    }
                }
                pw.println("");
                pw.flush();
            }
            pw.flush();
            pw.close();

            System.out.println("Total Number of Players from Savant: "+player_info_from_savant.size());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    private static void read_files_for_record_linkage(){
        player_info_from_schedule = new HashMap<String, String[]>();
        player_info_from_schedule_with_full_name = new HashMap<String, String[]>();
        player_info_from_savant = new HashMap<String, String[]>();

        try{
            FileInputStream fis = new FileInputStream("player_info_from_savant.csv");
            Scanner sc = new Scanner(fis);
            sc.nextLine();
            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");

                player_info_from_savant.put(spt[0], spt);
            }
            sc.close();
            fis.close();

            fis = null;
            if(new File("player_info_from_schedule_with_full_name.csv").exists()){
                fis = new FileInputStream("player_info_from_schedule_with_full_name.csv");
                with_full_name = true;
                sc = new Scanner(fis);
            }
            else{
                fis = new FileInputStream("player_info_from_schedule.csv");
                with_full_name = false;
                BufferedReader bf = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
                sc = new Scanner(bf);
            }
            sc.nextLine();
            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");

                player_info_from_schedule_with_full_name.put(spt[0], spt);
            }
            sc.close();
            fis.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void record_linkage(){
        HashMap<String, String[]> player_info_record_linkage = new HashMap<String, String[]>();
        HashMap<String, String> id_record_linkage = new HashMap<String, String>();

        int fail_to_link = 0;

        // The goal is to match from savant to schedule

        for(Object obj: player_info_from_savant.keySet()){
            String[] linked_player_info;
            if(!with_full_name){
                linked_player_info = new String[8];
            }
            else{
                linked_player_info = new String[10];
            }

            String[] this_player_info = player_info_from_savant.get(obj);

            linked_player_info[0] = this_player_info[0];
            linked_player_info[2] = this_player_info[1];
            linked_player_info[4] = this_player_info[2];
            linked_player_info[6] = this_player_info[3];

            String matched_id = "-1";
            boolean similarity_matching = false;

            for(Object obj_counter: player_info_from_schedule_with_full_name.keySet()){
                String[] counter_player_info = player_info_from_schedule_with_full_name.get(obj_counter);
                if(!this_player_info[3].equals(counter_player_info[3])){
                    continue;
                }
                if(this_player_info[1].split("_")[0].equals(counter_player_info[2].split(" ")[0])){
                    //System.out.println("Matched !");
                    matched_id = obj_counter.toString();
                }
            }

            // same team, but with one different character in name (encoding issue)
            if(matched_id.equals("-1")){
                int max_jaccard_counter = -1;
                for(Object obj_counter: player_info_from_schedule_with_full_name.keySet()){
                    String[] counter_player_info = player_info_from_schedule_with_full_name.get(obj_counter);
                    if(!this_player_info[3].equals(counter_player_info[3])){
                        continue;
                    }
                    if(this_player_info[1].split("_")[0].length() == counter_player_info[2].split(" ")[0].length()){
                        String this_name = this_player_info[1].split("_")[0];
                        String counter_name = counter_player_info[2].split(" ")[0];
                        int jaccard_counter = 0;
                        for(int name_counter=0; name_counter<this_name.length(); name_counter++){
                            if(this_name.charAt(name_counter) == counter_name.charAt(name_counter)){
                                jaccard_counter++;
                            }
                        }

                        if(jaccard_counter > max_jaccard_counter){
                            max_jaccard_counter = jaccard_counter;
                            if(max_jaccard_counter >= this_name.length()-1){
                                matched_id = obj_counter.toString();
                                similarity_matching = true;
                            }
                        }
                    }
                }
            }

            // same name, but in different team (not detected player transfer)
            if(matched_id.equals("-1")){
                for(Object obj_counter: player_info_from_schedule_with_full_name.keySet()){
                    String[] counter_player_info = player_info_from_schedule_with_full_name.get(obj_counter);
                    if(!this_player_info[1].split("_")[0].equals(counter_player_info[2].split(" ")[0])){
                        continue;
                    }

                    matched_id = obj_counter.toString();
                    similarity_matching = true;
                }
            }

            linked_player_info[1] = player_info_from_schedule_with_full_name.get(matched_id)[0];
            linked_player_info[3] = player_info_from_schedule_with_full_name.get(matched_id)[1];
            linked_player_info[5] = player_info_from_schedule_with_full_name.get(matched_id)[2];
            linked_player_info[7] = player_info_from_schedule_with_full_name.get(matched_id)[3];
            if(with_full_name){
                linked_player_info[8] = player_info_from_schedule_with_full_name.get(matched_id)[4];
                linked_player_info[9] = player_info_from_schedule_with_full_name.get(matched_id)[5];
            }

            for(int i=0; i<linked_player_info.length; i++){
                System.out.print(linked_player_info[i]+", ");
            }
            System.out.println("");

            player_info_record_linkage.put(obj.toString(), linked_player_info);
            id_record_linkage.put(obj.toString(), matched_id);
        }


        System.out.println("Fail to link: "+fail_to_link);


        try{
            PrintWriter pw = new PrintWriter("record_linkage.csv");
            if(!with_full_name){
                pw.println("savant_id,schedule_id,savant_uri,schedule_url,savant_name,schedule_name,savant_team,schedule_team");
            }
            else{
                pw.println("savant_id,schedule_id,savant_uri,schedule_url,savant_name,schedule_name,savant_team,schedule_team,official_name,full_name");
            }
            pw.flush();

            for(int obj=0; obj<player_info_record_linkage.size(); obj++){
                String[] print_player_info = player_info_record_linkage.get(obj+"");
                for(int info=0; info<print_player_info.length; info++){
                    pw.print(print_player_info[info]);
                    pw.flush();
                    if(info< print_player_info.length-1){
                        pw.print(",");
                        pw.flush();
                    }
                }
                pw.println("");
                pw.flush();
            }
            pw.flush();
            pw.close();

            pw = new PrintWriter("record_linkage_id.csv");
            pw.println("savant_uri,schedule_id,schedule_url");
            pw.flush();
            for(int obj=0; obj<player_info_record_linkage.size(); obj++){
                String[] print_player_info = player_info_record_linkage.get(obj+"");
                pw.println(print_player_info[2]+","+print_player_info[1]+","+print_player_info[3]);
                pw.flush();
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static HashMap<String, String> read_linkage;
    private static HashMap<String, String> read_linkage_rv;
    private static void read_record_linkage(){
        read_linkage = new HashMap<String, String>();
        read_linkage_rv = new HashMap<String, String>();
        try{
            FileInputStream fis = new FileInputStream("record_linkage_id.csv");
            Scanner sc = new Scanner(fis);
            sc.nextLine();

            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");
                read_linkage.put(spt[0], spt[1]);
                read_linkage_rv.put(spt[2], spt[1]);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void generate_team_schedule_result(){
        HashMap<Integer, String[]> team_schedule_result = new HashMap<Integer, String[]>();

        File directory = new File("Schedule");
        try {
            int reading_counter = 0;
            for(File sub_directory: directory.listFiles()){
                if(sub_directory.isDirectory()){
                    String game_general_info = sub_directory.getName();
                    //System.out.println("Schedule/"+game_general_info);

                    if(game_general_info.contains("All-Stars") || game_general_info.contains("-1")){
                        continue;
                    }

                    String game_time_info = game_general_info.substring(5, 10);
                    game_time_info = game_time_info.replace(" ","/");

                    String game_team_info = game_general_info.substring(11, game_general_info.length());
                    String[] game_team_name = game_team_info.split(" VS. ");

                    String home_team = game_team_name[0];
                    String away_team = game_team_name[1];
                    //System.out.println(away_team+" --- "+home_team);

                    FileInputStream fis = new FileInputStream("Schedule/"+game_general_info+"/"+"score.csv");
                    Scanner sc = new Scanner(fis);
                    String scoring = sc.nextLine();
                    scoring = scoring.replace(",","");
                    String[] scoring_spt = scoring.split(":");
                    String simple_result = "";

                    //System.out.println(game_general_info+"=>"+scoring);
                    if(Integer.parseInt(scoring_spt[1]) > Integer.parseInt(scoring_spt[0])){
                        simple_result = "WIN";
                    }
                    else{
                        simple_result = "LOSE";
                    }
                    String[] this_schedule_result = new String[8];
                    this_schedule_result[0] = game_team_name[0];
                    this_schedule_result[1] = game_time_info;
                    this_schedule_result[2] = game_team_name[1];
                    this_schedule_result[3] = simple_result;
                    this_schedule_result[4] = "HOME";
                    this_schedule_result[5] = scoring_spt[1];
                    this_schedule_result[6] = scoring_spt[0];
                    this_schedule_result[7] = game_general_info;
                    team_schedule_result.put(team_schedule_result.size(), this_schedule_result);


                    if(Integer.parseInt(scoring_spt[0]) > Integer.parseInt(scoring_spt[1])){
                        simple_result = "WIN";
                    }
                    else{
                        simple_result = "LOSE";
                    }
                    this_schedule_result = new String[8];
                    this_schedule_result[0] = game_team_name[1];
                    this_schedule_result[1] = game_time_info;
                    this_schedule_result[2] = game_team_name[0];
                    this_schedule_result[3] = simple_result;
                    this_schedule_result[4] = "AWAY";
                    this_schedule_result[5] = scoring_spt[0];
                    this_schedule_result[6] = scoring_spt[1];
                    this_schedule_result[7] = game_general_info;
                    team_schedule_result.put(team_schedule_result.size(), this_schedule_result);

                    sc.close();
                    fis.close();


                    reading_counter++;
                    if(reading_counter%100==0){
                        //System.out.println("Reading source file... ("+reading_counter+"/622)");
                    }
                }
            }


            PrintWriter pw = new PrintWriter("team_schedule_result.csv");
            pw.println("Id,Team,TimeInfo,Against,SimpleResult,SimpleLocation,ScoreSecured,ScoreGiven,GeneralInfo");
            pw.flush();
            for(Object obj: team_schedule_result.keySet()){
                String[] this_match_info = team_schedule_result.get(obj);
                pw.print(obj+",");
                pw.flush();
                for(int traverse_info=0; traverse_info<this_match_info.length; traverse_info++){
                    pw.print(this_match_info[traverse_info]);
                    pw.flush();

                    if(traverse_info<this_match_info.length-1){
                        pw.print(",");
                        pw.flush();
                    }
                }
                pw.println("");
                pw.flush();
            }
            pw.flush();
            pw.close();

            System.out.println("Total Number of Match Results from MLB website: "+team_schedule_result.size());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void generate_player_overall_performance(){
        File directory = new File("Data/player_individual");
        HashMap<String, String[]> player_overall_performance = new HashMap<String, String[]>();
        try{
            int reading_counter = 0;
            for(File sub_directory: directory.listFiles()){
                if(sub_directory.isDirectory()){
                    String player_name_abbr = sub_directory.getName();
                    String player_id = read_linkage.get(player_name_abbr);
                    boolean is_pitcher = false;

                    FileInputStream fis = new FileInputStream(sub_directory+"/"+"Stats.csv");
                    Scanner sc = new Scanner(fis);
                    String header = sc.nextLine();
                    String[] header_spt = header.split(",");
                    if(header_spt[1].equals("W")){
                        is_pitcher = true;
                    }

                    String performance = sc.nextLine();
                    String[] spt = performance.split(",");
                    String[] player_info;

                    if(is_pitcher){
                        player_info = new String[9+1];
                        player_info[0] = "Pitcher";
                    }
                    else{
                        player_info = new String[11+1];
                        player_info[0] = "Batter";
                    }

                    for(int index=0; index<player_info.length-1; index++){
                        player_info[index+1] = spt[index+1];
                    }
                    sc.close();
                    fis.close();

                    player_overall_performance.put(player_id, player_info);
                }

                reading_counter++;
                if(reading_counter%50==0){
                    //System.out.println("Reading source file... ("+reading_counter+"/174)");
                }
            }


            PrintWriter pw = new PrintWriter("player_overall_performance.csv");
            pw.println("Id,PlayerType,W,L,ERA,G,GS,SV,IP,SO,WHIP,PA,AB,R,H,HR,RBI,SB,AVG,OBP,SLG,OPS");
            pw.flush();
            for(Object obj: player_overall_performance.keySet()){
                String[] this_player_info = player_overall_performance.get(obj);
                pw.print(obj+","+this_player_info[0]);
                pw.flush();
                if(this_player_info[0].equals("Pitcher")){
                    for(int traverse_info=1; traverse_info<this_player_info.length; traverse_info++){
                        pw.print(","+this_player_info[traverse_info]);
                        pw.flush();
                    }
                    for(int skipping=0; skipping<11; skipping++){
                        pw.print(",");
                        pw.flush();
                    }
                }
                else{
                    for(int skipping=0; skipping<9; skipping++){
                        pw.print(",");
                        pw.flush();
                    }
                    for(int traverse_info=1; traverse_info<this_player_info.length; traverse_info++){
                        pw.print(","+this_player_info[traverse_info]);
                        pw.flush();
                    }
                }
                pw.println("");
                pw.flush();
            }
            pw.flush();
            pw.close();

            System.out.println("Total Number of Players from Savant: "+player_overall_performance.size());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void generate_player_savant_performance(){
        File directory = new File("Data/player_individual");
        HashMap<String, HashMap<String, String[]>> player_monthly_performance = new HashMap<String, HashMap<String, String[]>>();

        String[] candidate_split = {"Monthly","Game_Type","Platoon","Inning"};
        String[] candidate_split_batter = {"Monthly","Game_Type","Platoon","Inning"};
        String[] candidate_split_pitcher = {"Monthly","Base_Runner","Platoon","Outs"}; // deals with filename mismatch

        HashMap<String, Integer>[] type2value = new HashMap[4];
        for(int i=0; i<type2value.length; i++){
            type2value[i] = new HashMap<String, Integer>();
        }
        type2value[0].put("April",0);
        type2value[0].put("May",1);
        type2value[0].put("June",2);
        type2value[0].put("July",3);
        type2value[0].put("August",4);
        type2value[0].put("September",5);
        type2value[0].put("October",6);
        type2value[1].put("Home Games",0);
        type2value[1].put("Away Games",1);
        type2value[2].put("vs Left",0);
        type2value[2].put("vs Right",1);
        type2value[3].put("First Inning",0);
        type2value[3].put("Second Inning",1);
        type2value[3].put("Third Inning",2);
        type2value[3].put("Fourth Inning",3);
        type2value[3].put("Fifth Inning",4);
        type2value[3].put("Sixth Inning",5);
        type2value[3].put("Seventh Inning",6);
        type2value[3].put("Eighth Inning",7);
        type2value[3].put("Ninth Inning",8);
        type2value[3].put("Extra Innings",9);


        for(int candidate_id=0; candidate_id<candidate_split.length; candidate_id++){
            try{
                int reading_counter = 0;
                for(File sub_directory: directory.listFiles()){
                    if(sub_directory.isDirectory()){
                        String player_name_abbr = sub_directory.getName();
                        String player_id = read_linkage.get(player_name_abbr);
                        boolean is_pitcher = false;


                        FileInputStream fis = new FileInputStream(sub_directory+"/"+"Stats.csv");
                        Scanner sc = new Scanner(fis);
                        String header = sc.nextLine();
                        String[] header_spt = header.split(",");
                        if(header_spt[1].equals("W")){
                            is_pitcher = true;
                        }


                        if(is_pitcher){
                            fis = new FileInputStream(sub_directory+"/"+"Splits_MLB_"+candidate_split_pitcher[candidate_id]+"_Splits.csv");
                        }
                        else{
                            fis = new FileInputStream(sub_directory+"/"+"Splits_MLB_"+candidate_split_batter[candidate_id]+"_Splits.csv");
                        }
                        sc = new Scanner(fis);
                        sc.nextLine();

                        HashMap<String, String[]> monthly_info = new HashMap<String, String[]>();

                        HashSet<String> type = new HashSet<String>();
                        for(;sc.hasNextLine();){
                            String performance = sc.nextLine();
                            String[] spt = performance.split(",");
                            if(type.contains(spt[2]) || !type2value[candidate_id].containsKey(spt[2])){
                                continue;
                            }
                            type.add(spt[2]);

                            String[] player_info;
                            if(is_pitcher){
                                player_info = new String[15+2];
                                player_info[0] = "Pitcher";
                            }
                            else{
                                player_info = new String[17+2];
                                player_info[0] = "Batter";
                            }

                            for(int index=0; index<player_info.length-1; index++){
                                player_info[index+1] = spt[index+2];
                            }

                            monthly_info.put(spt[2], player_info);
                        }


                        sc.close();
                        fis.close();

                        player_monthly_performance.put(player_id, monthly_info);
                    }

                    reading_counter++;
                    if(reading_counter%50==0){
                        //System.out.println("Reading source file... ("+reading_counter+"/174)");
                    }
                }


                PrintWriter pw = new PrintWriter("player_overall_performance_split_"+candidate_split[candidate_id].toLowerCase()+".csv");
                pw.println("Id,PlayerType,MonthType,MonthTypeV,W,L,ERA,G,GS,SV,IP,BF,H,R,ER,HR,BB,SO,WHIP,PA,AB,R,H,2B,3B,HR,RBI,BB,SO,SB,CS,HBP,AVG,OBP,SLG,OPS");
                pw.flush();
                for(Object obj: player_monthly_performance.keySet()){
                    HashMap<String, String[]> this_player_info_m = player_monthly_performance.get(obj);
                    for(Object obj2: this_player_info_m.keySet()){
                        String[] this_player_info = this_player_info_m.get(obj2);
                        pw.print(obj+","+this_player_info[0]+","+this_player_info[1]+","+type2value[candidate_id].get(this_player_info[1]));
                        pw.flush();

                        if(this_player_info[0].equals("Pitcher")){
                            for(int traverse_info=2; traverse_info<this_player_info.length; traverse_info++){
                                pw.print(","+this_player_info[traverse_info]);
                                pw.flush();
                            }
                            for(int skipping=0; skipping<17; skipping++){
                                pw.print(",");
                                pw.flush();
                            }
                        }
                        else{
                            for(int skipping=0; skipping<15; skipping++){
                                pw.print(",");
                                pw.flush();
                            }
                            for(int traverse_info=2; traverse_info<this_player_info.length; traverse_info++){
                                pw.print(","+this_player_info[traverse_info]);
                                pw.flush();
                            }
                        }
                        pw.println("");
                        pw.flush();
                    }
                }
                pw.flush();
                pw.close();

                System.out.println("Total Number of Players from Savant: "+player_monthly_performance.size());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }


    }



    private static void generate_player_play_for_match(){
        HashMap<String, Integer> directory2id = new HashMap<String, Integer>();
        HashMap<Integer, String[]> player_in_match = new HashMap<Integer, String[]>();

        File directory = new File("Schedule");
        try {
            FileInputStream fis = new FileInputStream("team_schedule_result.csv");
            Scanner sc = new Scanner(fis);
            sc.nextLine();
            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");
                directory2id.put(spt[8]+"_"+spt[5].toLowerCase(), Integer.parseInt(spt[0]));
            }


            int reading_counter = 0;
            for(File sub_directory: directory.listFiles()){
                if(sub_directory.isDirectory()){
                    String game_general_info = sub_directory.getName();
                    //System.out.println("Schedule/"+game_general_info);

                    if(game_general_info.contains("All-Stars") || game_general_info.contains("-1")){
                        continue;
                    }

                    String game_time_info = game_general_info.substring(5, 10);
                    game_time_info = game_time_info.replace(" ","/");

                    String game_team_info = game_general_info.substring(11, game_general_info.length());
                    String[] game_team_name = game_team_info.split(" VS. ");

                    String home_team = game_team_name[0];
                    String away_team = game_team_name[1];
                    //System.out.println(away_team+" --- "+home_team);

                    String[] team_pos = {"away", "home"};
                    String[] player_pos = {"batter", "pitcher"};

                    for(int t_pos=0; t_pos<2; t_pos++){
                        for(int p_pos=0; p_pos<2; p_pos++){
                            fis = new FileInputStream("Schedule/"+game_general_info+"/"+team_pos[t_pos]+"_"+player_pos[p_pos]+".csv");
                            sc = new Scanner(fis);

                            sc.nextLine();
                            for(;sc.hasNextLine();){
                                String str = sc.nextLine();
                                String[] spt = str.split(",");
                                if(spt[0].length() > 0){
                                    if(!read_linkage_rv.containsKey(spt[0])){
                                        continue;
                                    }

                                    int get_this_match_id = directory2id.get(game_general_info+"_"+team_pos[1-t_pos]); // filename error // away <-> home

                                    String[] player_info;
                                    if(p_pos==1){
                                        player_info = new String[4+3];  // shall be 8+3, unknown file damage
                                        player_info[1] = "Pitcher";
                                    }
                                    else{
                                        player_info = new String[4+3]; // shall be 9+3, unknown file damage
                                        player_info[1] = "Batter";
                                    }
                                    player_info[0] = read_linkage_rv.get(spt[0]);
                                    player_info[2] = get_this_match_id+"";

                                    //System.out.println(str);
                                    for(int index=0; index<player_info.length-3; index++){
                                        player_info[index+3] = spt[index+3];
                                    }

                                    player_in_match.put(player_in_match.size(), player_info);
                                }
                            }
                            sc.close();
                            fis.close();
                        }
                    }

                    reading_counter++;
                    if(reading_counter%100==0){
                        //System.out.println("Reading source file... ("+reading_counter+"/622)");
                    }
                }
            }


            PrintWriter pw = new PrintWriter("player_in_match.csv");
            pw.println("Id,PlayerId,PlayerType,MatchId,IP,H,R,ER,AB,R,H,RBI");
            pw.flush();
            for(int obj=0; obj<player_in_match.size(); obj++){
                String[] this_player_info = player_in_match.get(obj);
                pw.print(obj+","+this_player_info[0]+","+this_player_info[1]+","+this_player_info[2]);
                pw.flush();

                if(this_player_info[1].equals("Pitcher")){
                    for(int traverse_info=3; traverse_info<this_player_info.length; traverse_info++){
                        pw.print(","+this_player_info[traverse_info]);
                        pw.flush();
                    }
                    for(int skipping=0; skipping<4; skipping++){ // 9
                        pw.print(",");
                        pw.flush();
                    }
                }
                else{
                    for(int skipping=0; skipping<4; skipping++){ // 8
                        pw.print(",");
                        pw.flush();
                    }
                    for(int traverse_info=3; traverse_info<this_player_info.length; traverse_info++){
                        pw.print(","+this_player_info[traverse_info]);
                        pw.flush();
                    }
                }
                pw.println("");
                pw.flush();

            }
            pw.flush();
            pw.close();

            System.out.println("Total Number of Player-in-Match Records: "+player_in_match.size());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
