import streamlit as st
import pandas as pd
import numpy as np
import re
import json
import os
import rdflib
import kglab

def main():
    page = st.sidebar.selectbox(
        "Select a Page",
        [
            "Homepage",
            "Search with Sparql",
            "Search with selection",
            "Predict",
            "Player Information",
            "Scheduel"

        ]
    )

    # First Page
    if page == "Homepage":
        homepage()


    #Search Page
    if page == "Search with Sparql":
        Search()

    if page == "Search with selection":
        Search2()
    #Predict Page
    if page == "Predict":
        Predict()
    #Player Information Page
    if page == "Player Information":
        Player()

    #Scheduel Page
    if page == "Scheduel":
        Scheduel()


def homepage():
    st.write("""
            # MLB KG
            
            ### This project is about to build a knowledge graph for MLB.
            
            #### This web was created by  ***Zihao H., Fandel Lin.***
            
            #""")

    st.write("""
    

    
    ```diff
    + Goals:
    
       In this project, we plan to build a knowledge graph about baseball games. 
       The knowledge graph will hold teams of a baseball league, match schedules 
       of teams, game results of matches, players of teams, historical records of 
       players, and statistics of players in matches. For different types (e.g., 
       pitchers and hitters) of players in a team, the statistics here span from 
       pitch types, spin direction, and pitch movement for pitchers; to pitch 
       tracking, plate discipline, and batted-ball profiles for hitters. This brings 
       a total of at least 11 semantic types.

     
       With visualization, this knowledge graph could help people dig into the 
       interactive and synergistic relationship among players in baseball games. 
       Furthermore, we plan to exploit this knowledge graph to provide a data-centric 
       approach to inferring the performance of players and the results of matches.

    ```
    """)


def Search():
    st.write("""
    # Search for query
    """)

    sample_query = st.selectbox('Select a sample query', ['All', 'Player belongs to which team?','How player (a hitter) performs in a match?'])
    if sample_query == 'All':
        st.caption('Copy sample query from the text box')
        s="""
        SELECT * WHERE {
        ?s ?p ?o .
        }
        """
        st.code(s,language='sparql')
        st.warning('Do NOT actually run this query, it will take over all the memory of the browser')

    elif sample_query == 'Player belongs to which team?':
        st.caption('Copy sample query from the text box')
        s="""
        SELECT ?person ?object_name
        WHERE {
            ?person <http://dsci558.org/ontology/belong_to_team> ?object.
            ?object <http://schema.org/name> ?object_name
        }
        """
        st.code(s, language='sparql')
    elif sample_query == 'How player (a hitter) performs in a match?':
        st.caption('Copy sample query from the text box')
        s="""
            SELECT DISTINCT ?match ?player1_name ?player_H_in_match ?player_RBI_in_match ?team1_name ?team2_name ?date ?location
            WHERE {
                ?match a <http://dsci558.org/class/Match>.
            
                ?match <http://dsci558.org/ontology/has_player_played> ?player1.
                ?match <http://dsci558.org/ontology/plays_at> ?location.
                ?match <http://dsci558.org/ontology/plays_on> ?date.
                ?match <http://dsci558.org/ontology/plays_by> ?team1.
                ?match <http://dsci558.org/ontology/plays_against> ?team2.
            
                ?player1 <http://dsci558.org/ontology/has_match> ?player_match.
                ?player_match <http://dsci558.org/ontology/has_status> ?player_status_in_match.
                ?player_status_in_match <http://dsci558.org/ontology/has_status_for_match> ?match.
                ?player_status_in_match <http://dsci558.org/ontology/has_H> ?player_H_in_match.
                ?player_status_in_match <http://dsci558.org/ontology/has_RBI> ?player_RBI_in_match.
                
                ?team1 <http://dsci558.org/ontology/has_player> ?player1_cluster.
                ?player1_cluster <http://dsci558.org/ontology/for_player> ?player1.
            
                ?player1 <http://schema.org/name> ?player1_name.
                ?team1 <http://schema.org/name> ?team1_name.
                ?team2 <http://schema.org/name> ?team2_name.
            }
            LIMIT 10
            """
        st.code(s, language='sparql')

    buff, col, buff2 = st.columns([1, 50, 1])


    sparql = """ {} """.format(col.text_area("Enter a query"))

    g = rdflib.Graph()
    g.parse('prototype.ttl', format="ttl")
    try:
        if sparql !=""" {} """.format(''):
            st.caption('this may take a while...')
            for row in g.query(sparql):
                st.write(row)
    except:
        st.error('Invalid query')

def Search2():
    namespaces = {
        'ppl': 'http://dsci558.org/player/',
        'loc': 'http://mylocations.org/addresses/',
        'schema': 'http://schema.org/',
        'ttm': 'http://dsci558.org/team/',
        'mmc': 'http://dsci558.org/match/',
        'ccs': 'http://dsci558.org/class/',
        'cct': 'http://dsci558.org/cluster/',
        'ont': 'http://dsci558.org/ontology/',
        'rdf0': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
        'rdfs': 'http://www.w3.org/2000/01/rdf-schema#'

    }
    kg = kglab.KnowledgeGraph(
        name="MLB KG example ",
        base_uri="http://dsci558.org/",
        namespaces=namespaces,
    )




    st.write("""Please select the class you want to search for""")
    g = rdflib.Graph()
    g.parse('prototype.ttl', format="ttl")
    classes=st.selectbox('Select a class', [' ', 'player','match','team'])
    if classes=='player':
        st.write("""
        ##### Search for a player with predicates
        """)
        predicate=st.selectbox('Select a predicate', ['belong_to_team', 'has_match','pos_bat','pos_throw','position'])
        if predicate=='belong_to_team':
            query="""
            SELECT ?person_name ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/belong_to_team> ?object.
                ?object <http://schema.org/name> ?object_name.
                ?person <http://schema.org/name> ?person_name
            }
            """

            pyvis_graph = kg.visualize_query(query, notebook=True)

            pyvis_graph.force_atlas_2based()

            pyvis_graph.show("test.html")
            HtmlFile = open("test.html", 'r', encoding='utf-8')
            source_code = HtmlFile.read()
            st.components.v1.html(source_code, height=300, width=300)

        for row in g.query(query):
                st.write('**{}** '.format(row.person_name)+'  plays   at   '+' *{}*'.format(row.object_name))




        if predicate=='has_match':
            query="""
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_match> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)

        if predicate=='pos_bat':
            query="""
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/pos_bat> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)

        if predicate=='pos_throw':
            query="""
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/pos_throw> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)

        if predicate=='position':
            query="""
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/position> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)

    if classes=='match':
        st.write("""
                ###### Search for a player with predicates
                """)
        predicate = st.selectbox('Select a predicate',
                                 ['ends_with', 'has_player_played', 'has_score_given', 'has_score_secured', 'plays_against','plays_at','plays_by','plays_on'])
        if predicate == 'ends_with':
            query = """
            SELECT ?t_name ?object
            WHERE {
                ?person <http://dsci558.org/ontology/ends_with> ?object.
                ?person  <http://dsci558.org/ontology/plays_by> ?t.
                ?t <http://schema.org/name> ?t_name
                
                
            }
            """
            col1, col2 = st.columns([40,60])

            teams= []
            end_with=[]
            with col1:
                for row in g.query(query):
                    st.write(row.t_name,row.object)
                    teams.append(row[0])
                    end_with.append(row[1])
            with col2:
                df = pd.DataFrame(np.array([teams, end_with]).T, columns=['teams', 'end_with'])
                df1=df.where(df['end_with'] == 'WIN').groupby(['teams']).count().set_axis(['WIN'], axis=1, copy=True)
                df2=df.where(df['end_with'] == 'LOSE').groupby(['teams']).count().set_axis(['LOSE'], axis=1, copy=True)

                st.bar_chart(df1.join(df2))





        if predicate == 'has_player_played':
            query = """
            SELECT ?t_name ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/has_player_played> ?object.
                ?object <http://schema.org/name> ?object_name.
                ?person  <http://dsci558.org/ontology/plays_by> ?t.
                ?t <http://schema.org/name> ?t_name
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'has_score_given':
            query = """
            SELECT ?object_name ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_score_given> ?object.
                ?person  <http://dsci558.org/ontology/plays_by> ?t.
                ?t <http://schema.org/name> ?object_name
            }
            """
            for row in g.query(query):
                st.write(row)

        if predicate == 'has_score_secured':
            query = """
            SELECT ?object_name ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_score_secured> ?object.
                ?person  <http://dsci558.org/ontology/plays_by> ?t.
                ?t <http://schema.org/name> ?object_name
                
            }
            """
            for row in g.query(query):
                st.write(row)



        if predicate == 'plays_against':
            query = """
            SELECT ?person ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/plays_against> ?object.
                ?object <http://schema.org/name> ?object_name
                
            }
            """
            for row in g.query(query):
                st.write(row)

        if predicate == 'plays_at':
            query = """
            SELECT ?object_name ?object
            WHERE {
                ?person <http://dsci558.org/ontology/plays_at> ?object.
                ?person  <http://dsci558.org/ontology/plays_by> ?t.
                ?t <http://schema.org/name> ?object_name
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'plays_by':
            query = """
            SELECT ?person ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/plays_by> ?object.
                ?object <http://schema.org/name> ?object_name
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'plays_on':
            query = """
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/plays_on> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)
    if classes=='team':
        st.write("""
                ###### Search for a player with predicates
                """)
        predicate = st.selectbox('Select a predicate',
                                 ['has_match', 'has_player'])
        if predicate == 'has_match':
            query = """
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_match> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'has_player':
            query = """
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_player> ?object.
                
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'has_score_given':
            query = """
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_score_given> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)

        if predicate == 'has_score_secured':
            query = """
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_score_secured> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'has_team_name':
            query = """
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/has_team_name> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)

def Player():

    st.write("""
    # Search for a player
    """)
    name= st.text_input("Enter a player name")
    path=''
    name_list=os.listdir(path+'statistics/player_individual')
    table_list = []
    for i in name_list:
        if len(re.findall(name, i, re.IGNORECASE)) > 0:
            table_list.append(i)

    for j in range(len(table_list)):
        player_path = path + 'statistics/player_individual/' + table_list[j] + '/'
        st.write('#### '+table_list[j])
        st.write('###### Stats')
        st.write(pd.read_csv(player_path + 'Stats.csv'))


def Predict():
    st.write("""
    # Predict
    """)
    g = rdflib.Graph()
    g.parse('prototype.ttl', format="ttl")
    sample_query = st.selectbox('Select a prediction query', [' ', 'Prediction - Game result','Prediction - Player Performance - How player (a hitter) performs in a match','Prediction - Player Performance - How player (a pitcher) performs in a match'])
    if sample_query == 'Prediction - Game result':
        query = """
        SELECT DISTINCT ?team1_name ?team2_name ?date ?location ?simple_result ?inference_simple_result
        WHERE {
            ?team1 <http://dsci558.org/ontology/has_player> ?player_cluster.
            ?team1 <http://dsci558.org/ontology/has_match> ?match_cluster.
            ?match_cluster <http://dsci558.org/ontology/for_match> ?match.
            ?match <http://dsci558.org/ontology/plays_at> ?location.
            ?match <http://dsci558.org/ontology/plays_on> ?date.
            ?match <http://dsci558.org/ontology/ends_with> ?simple_result.
            ?match <http://dsci558.org/ontology/plays_against> ?team2.
            ?match <http://dsci558.org/ontology/inference-ends_with> ?inference_simple_result.

            ?team1 <http://schema.org/name> ?team1_name.
            ?team2 <http://schema.org/name> ?team2_name.
        }
        LIMIT 10
        """
        st.code(query,language='sparql')

        for row in g.query(query):
            st.write(row.team1_name, '-(plays against)->', row.team2_name, '-(plays on)->', row.date, '-(plays at)->',
                  row.location, '-(ends with)->', row.simple_result, '<-(prediction)-', row.inference_simple_result)
    if sample_query == 'Prediction - Player Performance - How player (a hitter) performs in a match':
        query =  """
                    SELECT DISTINCT ?match ?player1_name ?inference_player_ER_in_match ?player_ER_in_match ?team1_name ?team2_name ?date ?location
                    WHERE {
                        ?match a <http://dsci558.org/class/Match>.
                    
                        ?match <http://dsci558.org/ontology/has_player_played> ?player1.
                        ?match <http://dsci558.org/ontology/plays_at> ?location.
                        ?match <http://dsci558.org/ontology/plays_on> ?date.
                        ?match <http://dsci558.org/ontology/plays_by> ?team1.
                        ?match <http://dsci558.org/ontology/plays_against> ?team2.
                    
                        ?player1 <http://dsci558.org/ontology/has_match> ?player_match.
                        ?player_match <http://dsci558.org/ontology/has_status> ?player_status_in_match.
                        ?player_status_in_match <http://dsci558.org/ontology/has_status_for_match> ?match.
                        ?player_status_in_match <http://dsci558.org/ontology/inference-has_ER> ?inference_player_ER_in_match.
                        ?player_status_in_match <http://dsci558.org/ontology/has_ER> ?player_ER_in_match.
                        
                        ?team1 <http://dsci558.org/ontology/has_player> ?player1_cluster.
                        ?player1_cluster <http://dsci558.org/ontology/for_player> ?player1.
                    
                        ?player1 <http://schema.org/name> ?player1_name.
                        ?team1 <http://schema.org/name> ?team1_name.
                        ?team2 <http://schema.org/name> ?team2_name.
                    }
                    LIMIT 10
                    """
        st.code(query,language='sparql')

        for row in g.query(query):
            st.write(row.player1_name, '<-(from)-',row.team1_name, '-(has ER in match)', row.player_ER_in_match , '<-(inference)-', row.inference_player_ER_in_match, '-(plays against)->', row.team2_name, '-(plays on)->', row.date, '-(plays at)->', row.location)

    if sample_query == 'Prediction - Player Performance - How player (a pitcher) performs in a match':
        query = """
        SELECT DISTINCT ?match ?player1_name ?inference_player_ER_in_match ?player_ER_in_match ?team1_name ?team2_name ?date ?location
        WHERE {
            ?match a <http://dsci558.org/class/Match>.

            ?match <http://dsci558.org/ontology/has_player_played> ?player1.
            ?match <http://dsci558.org/ontology/plays_at> ?location.
            ?match <http://dsci558.org/ontology/plays_on> ?date.
            ?match <http://dsci558.org/ontology/plays_by> ?team1.
            ?match <http://dsci558.org/ontology/plays_against> ?team2.

            ?player1 <http://dsci558.org/ontology/has_match> ?player_match.
            ?player_match <http://dsci558.org/ontology/has_status> ?player_status_in_match.
            ?player_status_in_match <http://dsci558.org/ontology/has_status_for_match> ?match.
            ?player_status_in_match <http://dsci558.org/ontology/inference-has_ER> ?inference_player_ER_in_match.
            ?player_status_in_match <http://dsci558.org/ontology/has_ER> ?player_ER_in_match.

            ?team1 <http://dsci558.org/ontology/has_player> ?player1_cluster.
            ?player1_cluster <http://dsci558.org/ontology/for_player> ?player1.

            ?player1 <http://schema.org/name> ?player1_name.
            ?team1 <http://schema.org/name> ?team1_name.
            ?team2 <http://schema.org/name> ?team2_name.
        }
        LIMIT 10
        """
        st.code(query,language='sparql')
        for row in g.query(query):
            st.write(row.player1_name, '<-(from)-',row.team1_name, '-(has ER in match)', row.player_ER_in_match , '<-(inference)-', row.inference_player_ER_in_match, '-(plays against)->', row.team2_name, '-(plays on)->', row.date, '-(plays at)->', row.location)



















#run the app
if __name__ == "__main__":
    main()