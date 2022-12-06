import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

public class inference {
    public static void main(String[] args) {
        initialization();

        // for match result
        read_schedule();
        read_savant();
        embedding();
        inference_for_match_result();
        //inference_for_match_result2();

        // for player performance
        read_match();
        inference_for_player_performance();
    }

    // The predictions shall be made with data earlier than the targeted timestamp.

    private static HashMap<String, Integer> team2id;
    private static String[] team_name = {"Chicago White Sox","Atlanta Braves","Milwaukee Brewers","Miami Marlins",
            "New York Mets","Houston Astros","Los Angeles Dodgers",
            "Oakland Athletics","St. Louis Cardinals","Tampa Bay Rays",
            "Minnesota Twins","Detroit Tigers","Washington Nationals",
            "Kansas City Royals","Toronto Blue Jays","Cleveland Guardians",
            "Colorado Rockies","Boston Red Sox","Philadelphia Phillies",
            "New York Yankees","Seattle Mariners","San Diego Padres",
            "San Francisco Giants","Cincinnati Reds","Arizona Diamondbacks",
            "Texas Rangers","Chicago Cubs","Pittsburgh Pirates","Baltimore Orioles",
            "Los Angeles Angels"};

    private static void initialization(){
        team2id = new HashMap<String, Integer>();
        for(int i=0; i<team_name.length; i++){
            team2id.put(team_name[i], i);
        }
    }


    private static HashMap<Integer, HashMap<Integer, String[]>> team_schedule;
    private static void read_schedule(){
        team_schedule = new HashMap<Integer, HashMap<Integer, String[]>>();
        try {
            FileInputStream fis = new FileInputStream("Source/team_schedule_result.csv");
            Scanner sc = new Scanner(fis);
            sc.nextLine();

            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");
                int this_match_id = Integer.parseInt(spt[0]);
                int this_team_id = team2id.get(spt[1]);
                int against_team_id = team2id.get(spt[3]);
                String match_result = spt[4];
                int match_result_bool = 0;
                if(match_result.equals("WIN")){
                    match_result_bool = 1;
                }
                String match_location = spt[5];
                int match_location_bool = 0;
                if(match_location.equals("HOME")){
                    match_location_bool = 1;
                }
                int match_score_diff = Integer.parseInt(spt[6]) - Integer.parseInt(spt[7]);
                String general_info = spt[8];
                String time_info = spt[2];

                String[] info_set = new String[10];
                info_set[0] = this_team_id+"";
                info_set[1] = this_match_id+"";
                info_set[2] = against_team_id+"";
                info_set[3] = match_result_bool+"";
                info_set[4] = match_location_bool+"";
                info_set[5] = match_score_diff+"";
                info_set[6] = spt[6];
                info_set[7] = spt[7];
                info_set[8] = general_info;
                info_set[9] = time_info;

                HashMap<Integer, String[]> info_set_placeholder = new HashMap<Integer, String[]>();
                if(team_schedule.containsKey(this_team_id)){
                    info_set_placeholder = team_schedule.get(this_team_id);
                }
                info_set_placeholder.put(info_set_placeholder.size(), info_set);
                team_schedule.put(this_team_id, info_set_placeholder);
            }
            sc.close();
            fis.close();


            /*
            PrintWriter pw = new PrintWriter("team_schedule_sorted.csv");
            pw.println("Id,TeamId,MatchId,AgainstTeamId,Result,Location,ScoreDiff,ScoreSecured,ScoreGiven,GeneralInfo,TimeInfo");
            pw.flush();
            int counter = 0;
            for(int i=0; i<team2id.size(); i++){
                for(int j=0; j<team_schedule.get(i).size(); j++){
                    pw.print(counter);
                    pw.flush();
                    for(int k=0; k<team_schedule.get(i).get(j).length; k++){
                        pw.print(","+team_schedule.get(i).get(j)[k]);
                        pw.flush();
                    }
                    pw.println("");
                    pw.flush();
                    counter++;
                }
            }
            pw.flush();
            pw.close();
            */
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }





    private static HashMap<Integer, Integer> player2team;
    private static HashMap<Integer, Integer> player2type;
    private static HashMap<Integer, HashMap<Integer, double[]>> player_performance_by_month;
    private static HashMap<Integer, HashMap<Integer, double[]>> team_performance_by_month;
    private static int[][][] team_has_player_counted;

    private static void read_savant(){
        player2team = new HashMap<Integer, Integer>();
        player2type = new HashMap<Integer, Integer>();
        player_performance_by_month = new HashMap<Integer, HashMap<Integer, double[]>>();
        team_performance_by_month = new HashMap<Integer, HashMap<Integer, double[]>>();

        team_has_player_counted = new int[team2id.size()][7][2];

        try {
            FileInputStream fis = new FileInputStream("Source/record_linkage.csv");
            Scanner sc = new Scanner(fis);
            sc.nextLine();

            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");
                player2team.put(Integer.parseInt(spt[1]), team2id.get(spt[7]));
            }
            sc.close();
            fis.close();


            fis = new FileInputStream("Source/player_overall_performance_split_monthly.csv");
            sc = new Scanner(fis);
            sc.nextLine();

            int temp_player_id = -1;
            HashMap<Integer, double[]> placeholder = new HashMap<Integer, double[]>();
            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");

                int this_player_id = Integer.parseInt(spt[0]);
                int this_team_id = player2team.get(this_player_id);
                String this_player_type = spt[1];
                int timeslot = Integer.parseInt(spt[3]);
                double[] info = new double[12];

                if(this_player_type.equals("Pitcher")){
                    info[0] = Double.parseDouble(spt[10]); // IP
                    info[1] = Double.parseDouble(spt[12]); // H
                    info[2] = Double.parseDouble(spt[13]); // R
                    info[3] = Double.parseDouble(spt[15]); // HR
                    info[4] = Double.parseDouble(spt[16]); // BB
                    info[5] = Double.parseDouble(spt[17]); // SO
                    player2type.put(this_player_id, 0);
                }
                else{
                    info[6] = Double.parseDouble(spt[22]); // H
                    info[7] = Double.parseDouble(spt[25]); // HR
                    info[8] = Double.parseDouble(spt[26]); // RBI
                    info[9] = Double.parseDouble(spt[27]); // BB
                    info[10] = Double.parseDouble(spt[28]); // SO
                    info[11] = Double.parseDouble(spt[32]); // AVG
                    player2type.put(this_player_id, 1);
                }

                if(this_player_id!=temp_player_id){
                    if(temp_player_id!=-1){
                        player_performance_by_month.put(temp_player_id, placeholder);
                    }
                    temp_player_id = this_player_id;
                    placeholder = new HashMap<Integer, double[]>();
                }

                placeholder.put(timeslot, info);
                if(this_player_type.equals("Pitcher")){
                    team_has_player_counted[this_team_id][timeslot][0]++;
                }
                else{
                    team_has_player_counted[this_team_id][timeslot][1]++;
                }
            }
            player_performance_by_month.put(temp_player_id, placeholder);
            sc.close();
            fis.close();


            /*
            for(int i=0; i<team2id.size(); i++){
                for(int j=0; j<6; j++){
                    System.out.print(team_has_player_counted[i][j][0]+","+team_has_player_counted[i][j][1]+",");
                }
                System.out.println("");
            }
            */




            for(int i=0; i<team2id.size(); i++){
                placeholder = new HashMap<Integer, double[]>();
                for(int j=0; j<6; j++){
                    placeholder.put(placeholder.size(), new double[12]);
                }
                team_performance_by_month.put(i, placeholder);
            }



            for(Integer obj: player_performance_by_month.keySet()){
                int this_player_id = obj;
                int this_team_id = player2team.get(this_player_id);

                HashMap<Integer, double[]> this_team_by_month = team_performance_by_month.get(this_team_id);
                for(int j=0; j<6; j++){
                    double[] this_team_this_month = this_team_by_month.get(j);
                    if(!player_performance_by_month.get(this_player_id).containsKey(j)){
                        continue;
                    }
                    /*
                    double[] this_player_this_month = player_performance_by_month.get(this_player_id).get(j);
                    System.out.println(this_player_id+", "+j+" => ");
                    System.out.println(this_player_this_month.length);
                    for(int k=0; k<12; k++){
                        System.out.print(this_player_this_month[k]+",");
                    }
                    System.out.println("");
                    */

                    for(int k=0; k<12; k++){
                        if(team_has_player_counted[this_team_id][j][k/6] == 0){
                            continue;
                        }
                        this_team_this_month[k] += player_performance_by_month.get(this_player_id).get(j)[k]/team_has_player_counted[this_team_id][j][k/6];
                    }

                    this_team_by_month.put(j, this_team_this_month);
                }
                team_performance_by_month.put(this_team_id, this_team_by_month);
            }




            /*
            for(Integer obj: team_performance_by_month.keySet()){
                int this_team_id = obj;
                HashMap<Integer, double[]> this_team_by_month = team_performance_by_month.get(this_team_id);
                System.out.println("Team: "+this_team_id);
                for(int j=0; j<6; j++){
                    System.out.print("Month: "+j+"  ");
                    double[] this_team_this_month = this_team_by_month.get(j);
                    for(int k=0; k<12; k++){
                        System.out.print(","+this_team_this_month[k]);
                    }
                    System.out.println();
                }
            }
            */
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    private static double[][][] team_similarity;
    private static HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> player_similarity;
    private static void embedding(){
        team_similarity = new double[team2id.size()][team2id.size()][6];

        for(Integer obj: team_performance_by_month.keySet()){
            int this_team_id = obj;
            HashMap<Integer, double[]> this_team_by_month = team_performance_by_month.get(this_team_id);

            for(Integer obj2: team_performance_by_month.keySet()){
                if(obj2 <= obj){
                    continue;
                }

                int this_team_id2 = obj2;
                HashMap<Integer, double[]> this_team_by_month2 = team_performance_by_month.get(this_team_id2);


                for(int j=0; j<6; j++){
                    double similarity_eval = 0.0;

                    double[] this_team_this_month = this_team_by_month.get(j);
                    double[] this_team_this_month2 = this_team_by_month2.get(j);

                    for(int k=0; k<12; k++){
                        if(k==11){
                            similarity_eval += Math.pow(this_team_this_month[k]-this_team_this_month2[k], 1);
                        }
                        else if(k==3 || k==7){
                            similarity_eval += 5.0*Math.pow(this_team_this_month[k]-this_team_this_month2[k], 2);
                        }
                        else{
                            similarity_eval += Math.pow(this_team_this_month[k]-this_team_this_month2[k], 2);
                        }
                    }
                    similarity_eval = Math.pow(similarity_eval, 0.5);
                    team_similarity[obj][obj2][j] = similarity_eval;
                    team_similarity[obj2][obj][j] = similarity_eval;
                }
            }
        }


        try{
            PrintWriter pw = new PrintWriter("embedding_similarity_team.csv");
            pw.println("TeamSource1,TeamSource2,EmbeddingSimilarity0,EmbeddingSimilarity1,EmbeddingSimilarity2,EmbeddingSimilarity3,EmbeddingSimilarity4,EmbeddingSimilarity5");
            pw.flush();
            for(int i=0; i<team2id.size(); i++){
                for(int j=0; j<team2id.size(); j++){
                    if(i==j){
                        continue;
                    }
                    System.out.print("TEAM SIMILARITY ["+i+"]["+j+"]: ");
                    pw.print(i+","+j);
                    pw.flush();
                    for(int k=0; k<6; k++){
                        System.out.print(team_similarity[i][j][k]+",");
                        pw.print(","+team_similarity[i][j][k]);
                        pw.flush();
                    }
                    System.out.println("");
                    pw.println("");
                    pw.flush();
                }
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }




        player_similarity = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();

        for(Integer obj: player_performance_by_month.keySet()){
            int this_player_id = obj;
            HashMap<Integer, double[]> this_player_by_month = player_performance_by_month.get(this_player_id);


            HashMap<Integer, HashMap<Integer, Double>> placeholder_against = new HashMap<Integer, HashMap<Integer, Double>>();
            for(Integer obj2: player_performance_by_month.keySet()){
                if(obj2 == obj){
                    continue;
                }
                if(!player2type.get(obj).equals(player2type.get(obj2))){
                    continue;
                }

                int this_player_id2 = obj2;
                HashMap<Integer, double[]> this_player_by_month2 = player_performance_by_month.get(this_player_id2);

                HashMap<Integer, Double> placeholder_month = new HashMap<Integer, Double>();
                for(int j=0; j<6; j++){
                    double similarity_eval = 0.0;

                    if(!this_player_by_month.containsKey(j) || !this_player_by_month2.containsKey(j)){
                        continue;
                    }

                    double[] this_player_this_month = this_player_by_month.get(j);
                    double[] this_player_this_month2 = this_player_by_month2.get(j);

                    for(int k=0; k<12; k++){
                        if(k==11){
                            similarity_eval += Math.pow(this_player_this_month[k]-this_player_this_month2[k], 1);
                        }
                        else if(k==3 || k==7){
                            similarity_eval += 5.0*Math.pow(this_player_this_month[k]-this_player_this_month2[k], 2);
                        }
                        else{
                            similarity_eval += Math.pow(this_player_this_month[k]-this_player_this_month2[k], 2);
                        }
                    }
                    similarity_eval = Math.pow(similarity_eval, 0.5);


                    placeholder_month.put(j, similarity_eval);
                }
                placeholder_against.put(this_player_id2, placeholder_month);
            }
            player_similarity.put(this_player_id, placeholder_against);
        }


        try{
            PrintWriter pw = new PrintWriter("embedding_similarity_player.csv");
            pw.println("PlayerSource1,PlayerSource2,EmbeddingSimilarity0,EmbeddingSimilarity1,EmbeddingSimilarity2,EmbeddingSimilarity3,EmbeddingSimilarity4,EmbeddingSimilarity5");
            pw.flush();
            System.out.println(player_performance_by_month.size());
            for(Integer i: player_performance_by_month.keySet()){
                for(Integer j: player_performance_by_month.keySet()){
                    if(i.equals(j)){
                        continue;
                    }
                    if(!player_similarity.containsKey(i) || !player_similarity.get(i).containsKey(j)){
                        continue;
                    }
                    System.out.print("PLAYER SIMILARITY ["+i+"]["+j+"]: ");
                    pw.print(i+","+j);
                    pw.flush();
                    for(int k=0; k<6; k++){
                        if(!player_similarity.get(i).get(j).containsKey(k) || player_similarity.get(i).get(j).get(k) == null){
                            System.out.print("100.0,");
                            pw.print(",100.0");
                            pw.flush();
                        }
                        else{
                            System.out.print(player_similarity.get(i).get(j).get(k)+",");
                            pw.print(","+player_similarity.get(i).get(j).get(k));
                            pw.flush();
                        }
                    }
                    System.out.println("");
                    pw.println("");
                    pw.flush();
                }
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    // sliding window for inference
    private static HashMap<Integer, HashMap<Integer, double[]>> expected_score;
    private static int window_size = 200;
    private static double decay_factor = 1.0;

    private static HashMap<String, String> inference_match_result;

    private static void inference_for_match_result(){
        inference_match_result = new HashMap<String, String>();
        int counting_hit = 0;
        int counting_miss = 0;

        int true_positive = 0;
        int true_negative = 0;
        int false_positve = 0;
        int false_negative = 0;


        for(int window=0; window<6; window++){
            int min_match_id = 0 + window*window_size;
            int max_match_id = window_size + window*window_size;

            expected_score = new HashMap<Integer, HashMap<Integer, double[]>>();

            for(int i=0; i<team2id.size(); i++){
                double[][] scores = new double[2][2];
                int[] valid_count = new int[2];
                for(int j=0; j<team_schedule.get(i).size(); j++){
                    if(Integer.parseInt(team_schedule.get(i).get(j)[1]) >= max_match_id || Integer.parseInt(team_schedule.get(i).get(j)[1]) < min_match_id){
                        continue;
                    }

                    decay_factor = ((Double.parseDouble(team_schedule.get(i).get(j)[1])-(double)min_match_id)/(double)window_size);
                    valid_count[Integer.parseInt(team_schedule.get(i).get(j)[4])] += decay_factor;
                    scores[Integer.parseInt(team_schedule.get(i).get(j)[4])][0] += Double.parseDouble(team_schedule.get(i).get(j)[6])*decay_factor;
                    scores[Integer.parseInt(team_schedule.get(i).get(j)[4])][1] += Double.parseDouble(team_schedule.get(i).get(j)[7])*decay_factor;
                }
                if(valid_count[0] > 0){
                    scores[0][0] = scores[0][0]/valid_count[0];
                    scores[0][1] = scores[0][1]/valid_count[0];
                }
                if(valid_count[1] > 0){
                    scores[1][0] = scores[1][0]/valid_count[1];
                    scores[1][1] = scores[1][1]/valid_count[1];
                }

                //System.out.println(scores[0][0]+", "+scores[0][1]+", "+scores[1][0]+", "+scores[1][1]);
                HashMap<Integer, double[]> placeholder = new HashMap<Integer, double[]>();
                placeholder.put(0, scores[0]);
                placeholder.put(1, scores[1]);
                expected_score.put(i, placeholder);
            }


            int max_match_id_testing = window_size*2 + window*window_size;




            for(int i=0; i<team2id.size(); i++){
                for(int j=0; j<team_schedule.get(i).size(); j++){
                    if(Integer.parseInt(team_schedule.get(i).get(j)[1]) >= max_match_id_testing || Integer.parseInt(team_schedule.get(i).get(j)[1]) < max_match_id){
                        continue;
                    }

                    int this_team = Integer.parseInt(team_schedule.get(i).get(j)[0]);
                    int against_team = Integer.parseInt(team_schedule.get(i).get(j)[2]);
                    int this_location = Integer.parseInt(team_schedule.get(i).get(j)[4]);
                    int ans_result = Integer.parseInt(team_schedule.get(i).get(j)[3]);
                    int ans_diff = Integer.parseInt(team_schedule.get(i).get(j)[5]);

                    double self_diff = expected_score.get(this_team).get(this_location)[0] - expected_score.get(this_team).get(this_location)[1];
                    double against_diff = expected_score.get(against_team).get(1-this_location)[0] - expected_score.get(against_team).get(1-this_location)[1];
                    int binary_prediction = 0;
                    self_diff = self_diff - against_diff;

                    double acc_to_self = 0.0;
                    double acc_to_against = 0.0;
                    for(int other_team=0; other_team<team2id.size(); other_team++){
                        if(other_team==this_team || other_team==against_team){
                            continue;
                        }
                        double similar_diff = expected_score.get(other_team).get(this_location)[0] - expected_score.get(other_team).get(this_location)[1];
                        double self_to_similar_diff = similar_diff*(1.0/team_similarity[this_team][other_team][window]);
                        double against_to_similar_diff = similar_diff*(1.0/team_similarity[against_team][other_team][window]);

                        acc_to_self += self_to_similar_diff;
                        acc_to_against += against_to_similar_diff;
                    }
                    //acc_to_self = acc_to_self/(team2id.size()-2);
                    //acc_to_against = acc_to_against/(team2id.size()-2);
                    //System.out.println(acc_to_self+" --- "+acc_to_against);

                    //self_diff = acc_to_self - acc_to_against;




                    if(self_diff >= 2.0 || (acc_to_self - acc_to_against) >= -2.0){
                        binary_prediction = 1;
                    }

                    /*
                    if(Math.random() > 0.5){
                        binary_prediction = 1;
                    }
                    */

                    if(binary_prediction==1){
                        inference_match_result.put(team_schedule.get(i).get(j)[1], "WIN");
                    }
                    else{
                        inference_match_result.put(team_schedule.get(i).get(j)[1], "LOSE");
                    }


                    System.out.println("Groundtruth: "+ans_result+"("+ans_diff+") => Inference:"+binary_prediction+"("+self_diff+")");

                    if(ans_result == binary_prediction){
                        counting_hit++;
                        if(ans_result == 1 && binary_prediction == 1){
                            true_positive++;
                        }
                        else{
                            true_negative++;
                        }
                    }
                    else{
                        counting_miss++;
                        if(ans_result == 0 && binary_prediction == 1){
                            false_positve++;
                        }
                        else if(ans_result == 1 && binary_prediction == 0){
                            false_negative++;
                        }
                    }
                }
            }

        }

        System.out.println("hit: "+counting_hit+"    miss: "+counting_miss+"    TP: "+true_positive+"    TN: "+true_negative+"    FP: "+false_positve+"    FN: "+false_negative);
        double precision = (double)true_positive/(double)(true_positive+false_positve);
        double recall = (double)true_positive/(double)(true_positive+false_negative);
        double f1_score = 2*(precision*recall)/(precision+recall);
        System.out.println("F1 Score: "+f1_score);
        System.out.println("Accuracy: "+((double)counting_hit/(double)(counting_hit+counting_miss)));

        try{
            PrintWriter pw = new PrintWriter("inference_result_match.csv");
            pw.println("MatchId,InferenceResult");
            pw.flush();
            for(String obj: inference_match_result.keySet()){
                pw.println(obj+","+inference_match_result.get(obj));
                pw.flush();
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }





    private static void inference_for_match_result2(){
        inference_match_result = new HashMap<String, String>();
        int counting_hit = 0;
        int counting_miss = 0;

        int true_positive = 0;
        int true_negative = 0;
        int false_positve = 0;
        int false_negative = 0;


        for(int window=0; window<6; window++){
            int min_match_id = 0 + window*window_size;
            int max_match_id = window_size + window*window_size;

            expected_score = new HashMap<Integer, HashMap<Integer, double[]>>();

            for(int i=0; i<team2id.size(); i++){
                double[][] scores = new double[2][2];
                int[] valid_count = new int[2];
                for(int j=0; j<team_schedule.get(i).size(); j++){
                    if(Integer.parseInt(team_schedule.get(i).get(j)[1]) >= max_match_id || Integer.parseInt(team_schedule.get(i).get(j)[1]) < min_match_id){
                        continue;
                    }

                    decay_factor = ((Double.parseDouble(team_schedule.get(i).get(j)[1])-(double)min_match_id)/(double)window_size);
                    valid_count[Integer.parseInt(team_schedule.get(i).get(j)[4])] += decay_factor;
                    scores[Integer.parseInt(team_schedule.get(i).get(j)[4])][0] += Double.parseDouble(team_schedule.get(i).get(j)[6])*decay_factor;
                    scores[Integer.parseInt(team_schedule.get(i).get(j)[4])][1] += Double.parseDouble(team_schedule.get(i).get(j)[7])*decay_factor;
                }
                if(valid_count[0] > 0){
                    scores[0][0] = scores[0][0]/valid_count[0];
                    scores[0][1] = scores[0][1]/valid_count[0];
                }
                if(valid_count[1] > 0){
                    scores[1][0] = scores[1][0]/valid_count[1];
                    scores[1][1] = scores[1][1]/valid_count[1];
                }

                //System.out.println(scores[0][0]+", "+scores[0][1]+", "+scores[1][0]+", "+scores[1][1]);
                HashMap<Integer, double[]> placeholder = new HashMap<Integer, double[]>();
                placeholder.put(0, scores[0]);
                placeholder.put(1, scores[1]);
                expected_score.put(i, placeholder);
            }


            int max_match_id_testing = window_size*2 + window*window_size;




            for(int i=0; i<team2id.size(); i++){
                for(int j=0; j<team_schedule.get(i).size(); j++){
                    if(Integer.parseInt(team_schedule.get(i).get(j)[1]) >= max_match_id_testing || Integer.parseInt(team_schedule.get(i).get(j)[1]) < max_match_id){
                        continue;
                    }

                    if(inference_match_result.containsKey(team_schedule.get(i).get(j)[1])){
                        continue;
                    }

                    int this_team = Integer.parseInt(team_schedule.get(i).get(j)[0]);
                    int against_team = Integer.parseInt(team_schedule.get(i).get(j)[2]);
                    int this_location = Integer.parseInt(team_schedule.get(i).get(j)[4]);
                    int ans_result = Integer.parseInt(team_schedule.get(i).get(j)[3]);
                    int ans_diff = Integer.parseInt(team_schedule.get(i).get(j)[5]);

                    double self_diff = expected_score.get(this_team).get(this_location)[0] - expected_score.get(this_team).get(this_location)[1];
                    double against_diff = expected_score.get(against_team).get(1-this_location)[0] - expected_score.get(against_team).get(1-this_location)[1];
                    int binary_prediction = 0;
                    self_diff = self_diff - against_diff;

                    double acc_to_self = 0.0;
                    double acc_to_against = 0.0;
                    for(int other_team=0; other_team<team2id.size(); other_team++){
                        if(other_team==this_team || other_team==against_team){
                            continue;
                        }
                        double similar_diff = expected_score.get(other_team).get(this_location)[0] - expected_score.get(other_team).get(this_location)[1];
                        double self_to_similar_diff = similar_diff*(1.0/team_similarity[this_team][other_team][window]);
                        double against_to_similar_diff = similar_diff*(1.0/team_similarity[against_team][other_team][window]);

                        acc_to_self += self_to_similar_diff;
                        acc_to_against += against_to_similar_diff;
                    }
                    //acc_to_self = acc_to_self/(team2id.size()-2);
                    //acc_to_against = acc_to_against/(team2id.size()-2);
                    //System.out.println(acc_to_self+" --- "+acc_to_against);

                    //self_diff = acc_to_self - acc_to_against;

                    double prediction = self_diff + (acc_to_self-acc_to_against);
                    //prediction = (acc_to_self-acc_to_against);
                    //prediction = Math.random();

                    if(prediction > 0.5){
                        inference_match_result.put(team_schedule.get(i).get(j)[1], "WIN");
                        binary_prediction = 1;
                        if(Integer.parseInt(team_schedule.get(i).get(j)[1])%2 == 0){
                            inference_match_result.put((Integer.parseInt(team_schedule.get(i).get(j)[1])+1)+"", "LOSE");
                        }
                        else{
                            inference_match_result.put((Integer.parseInt(team_schedule.get(i).get(j)[1])-1)+"", "LOSE");
                        }
                    }
                    else{
                        inference_match_result.put(team_schedule.get(i).get(j)[1], "LOSE");
                        binary_prediction = 0;
                        if(Integer.parseInt(team_schedule.get(i).get(j)[1])%2 == 0){
                            inference_match_result.put((Integer.parseInt(team_schedule.get(i).get(j)[1])+1)+"", "WIN");
                        }
                        else{
                            inference_match_result.put((Integer.parseInt(team_schedule.get(i).get(j)[1])-1)+"", "WIN");
                        }
                    }


                    System.out.println("Groundtruth: "+ans_result+"("+ans_diff+") => Inference:"+binary_prediction+"("+self_diff+")");

                    if(ans_result == binary_prediction){
                        counting_hit = counting_hit+2;
                        if(ans_result == 1 && binary_prediction == 1){
                            true_positive++;
                        }
                        else{
                            true_negative++;
                        }
                    }
                    else{
                        counting_miss = counting_miss+2;
                        if(ans_result == 0 && binary_prediction == 1){
                            false_positve++;
                        }
                        else if(ans_result == 1 && binary_prediction == 0){
                            false_negative++;
                        }
                    }
                }
            }

        }

        System.out.println("hit: "+counting_hit+"    miss: "+counting_miss+"    TP: "+true_positive+"    TN: "+true_negative+"    FP: "+false_positve+"    FN: "+false_negative);
        double precision = (double)true_positive/(double)(true_positive+false_positve);
        double recall = (double)true_positive/(double)(true_positive+false_negative);
        double f1_score = 2*(precision*recall)/(precision+recall);
        System.out.println("F1 Score: "+f1_score);
        System.out.println("Accuracy: "+((double)counting_hit/(double)(counting_hit+counting_miss)));

        try{
            PrintWriter pw = new PrintWriter("inference_result_match_v2.csv");
            pw.println("MatchId,InferenceResult");
            pw.flush();
            for(String obj: inference_match_result.keySet()){
                pw.println(obj+","+inference_match_result.get(obj));
                pw.flush();
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }












    private static HashMap<Integer, HashMap<Integer, String[]>> player_schedule;
    private static void read_match(){
        player_schedule = new HashMap<Integer, HashMap<Integer, String[]>>();
        try {
            FileInputStream fis = new FileInputStream("Source/player_in_match.csv");
            Scanner sc = new Scanner(fis);
            sc.nextLine();

            for(;sc.hasNextLine();){
                String str = sc.nextLine();
                String[] spt = str.split(",");
                int this_player_id = Integer.parseInt(spt[1]);
                int this_match_id = Integer.parseInt(spt[3]);
                int this_team_id = player2team.get(this_player_id);
                String player_type = spt[2];
                double targeted_info;
                if(player_type.equals("Batter")){
                    targeted_info = Double.parseDouble(spt[11]); // RBI
                }
                else{
                    targeted_info = Double.parseDouble(spt[7]); // ER
                }

                String[] info_set = new String[5];
                info_set[0] = this_player_id+"";
                info_set[1] = this_team_id+"";
                info_set[2] = this_match_id+"";
                info_set[3] = player_type+"";
                info_set[4] = targeted_info+"";

                HashMap<Integer, String[]> info_set_placeholder = new HashMap<Integer, String[]>();
                if(player_schedule.containsKey(this_player_id)){
                    info_set_placeholder = player_schedule.get(this_player_id);
                }
                info_set_placeholder.put(info_set_placeholder.size(), info_set);
                player_schedule.put(this_player_id, info_set_placeholder);
            }
            sc.close();
            fis.close();



            PrintWriter pw = new PrintWriter("player_schedule_sorted.csv");
            pw.println("Id,PlayerId,TeamId,MatchId,PlayerType,InferenceTarget");
            pw.flush();
            int counter = 0;
            for(Integer i: player_schedule.keySet()){
                for(int j=0; j<player_schedule.get(i).size(); j++){
                    pw.print(counter);
                    pw.flush();
                    for(int k=0; k<player_schedule.get(i).get(j).length; k++){
                        pw.print(","+player_schedule.get(i).get(j)[k]);
                        pw.flush();
                    }
                    pw.println("");
                    pw.flush();
                    counter++;
                }
            }
            pw.flush();
            pw.close();

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



    private static HashMap<Integer, Double> expected_reward;
    private static HashMap<String, HashMap<String, String>> inference_player_performance;
    private static HashMap<String, String> player2type_for_output;
    private static void inference_for_player_performance(){
        inference_player_performance = new HashMap<String, HashMap<String, String>>();
        player2type_for_output = new HashMap<String, String>();
        double mse = 0.0;
        double mse_counter = 0.0;


        for(int window=0; window<6; window++){
            int min_match_id = 0 + window*window_size;
            int max_match_id = window_size + window*window_size;

            expected_reward = new HashMap<Integer, Double>();

            decay_factor = 1.0;
            for(Integer i: player_performance_by_month.keySet()){
                double scores = 0.0;
                int valid_count = 0;
                for(int j=0; j<player_schedule.get(i).size(); j++){
                    if(Integer.parseInt(player_schedule.get(i).get(j)[2]) >= max_match_id || Integer.parseInt(player_schedule.get(i).get(j)[2]) < min_match_id){
                        continue;
                    }

                    decay_factor = 0.5+0.5*(((Double.parseDouble(player_schedule.get(i).get(j)[2])-(double)min_match_id)/(double)window_size));
                    valid_count += decay_factor;
                    scores += Double.parseDouble(player_schedule.get(i).get(j)[4])*decay_factor;
                }
                scores = scores/valid_count;
                if(valid_count == 0){
                    scores = 0.0;
                }

                System.out.println("Expected Reward: "+i+" => "+scores);
                expected_reward.put(i, scores);
            }


            int max_match_id_testing = window_size*2 + window*window_size;




            for(Integer i: player_performance_by_month.keySet()){
                for(int j=0; j<player_schedule.get(i).size(); j++){
                    if(Integer.parseInt(player_schedule.get(i).get(j)[2]) >= max_match_id_testing || Integer.parseInt(player_schedule.get(i).get(j)[2]) < max_match_id){
                        continue;
                    }

                    int this_player = Integer.parseInt(player_schedule.get(i).get(j)[0]);
                    int this_team = Integer.parseInt(player_schedule.get(i).get(j)[1]);
                    int this_match = Integer.parseInt(player_schedule.get(i).get(j)[2]);
                    String this_type = player_schedule.get(i).get(j)[3];
                    player2type_for_output.put(this_player+"", this_type);

                    double this_ans = Double.parseDouble(player_schedule.get(i).get(j)[4]);

                    double self_reward = expected_reward.get(this_player);

                    double prediction = 0.0;

                    double acc_to_self = 0.0;
                    double acc_to_self_counter = 0.0;
                    for(Integer other_player: player_performance_by_month.keySet()){
                        if(other_player==i){
                            continue;
                        }
                        if(!player_similarity.get(i).containsKey(other_player) || !player_similarity.get(i).get(other_player).containsKey(window)){
                            continue;
                        }
                        double similar_reward = (expected_reward.get(other_player) - self_reward);
                        //double similar_reward = expected_reward.get(other_player);
                        double self_to_similar_diff = similar_reward*(1.0/player_similarity.get(i).get(other_player).get(window));

                        acc_to_self += self_to_similar_diff;
                        acc_to_self_counter += 1.0/player_similarity.get(i).get(other_player).get(window);
                    }
                    acc_to_self = acc_to_self / acc_to_self_counter;

                    if(acc_to_self_counter == 0.0){
                        prediction = self_reward;
                    }
                    else{
                        prediction = self_reward+acc_to_self;
                    }

                    if(prediction < 0.0){
                        prediction = 0.0;
                    }

                    prediction = prediction;
                    prediction = Math.round(prediction);


                    double avg = 0.615538;
                    double median = 0.0;


                    HashMap<String, String> placeholder = new HashMap<String, String>();
                    if(inference_player_performance.containsKey(this_player+"")){
                        placeholder = inference_player_performance.get(this_player+"");
                    }
                    placeholder.put(this_match+"", prediction+"");
                    inference_player_performance.put(this_player+"", placeholder);


                    System.out.println("Groundtruth: "+this_ans+" => Inference:"+prediction);
                    double error = Math.abs(this_ans-prediction);
                    mse += error;
                    mse_counter += 1.0;
                }
            }

        }

        mse = mse/mse_counter;
        System.out.println("MSE: "+mse);

        try{
            PrintWriter pw = new PrintWriter("inference_result_player.csv");
            pw.println("PlayerId,PlayerType,MatchId,InferenceResult");
            pw.flush();
            for(String obj: inference_player_performance.keySet()){
                for(String obj2: inference_player_performance.get(obj).keySet()){
                    pw.println(obj+","+player2type_for_output.get(obj)+","+obj2+","+inference_player_performance.get(obj).get(obj2));
                    pw.flush();
                }
            }
            pw.flush();
            pw.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }




}
