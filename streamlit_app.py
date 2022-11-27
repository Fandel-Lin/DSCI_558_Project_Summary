import streamlit as st
import pandas as pd
import numpy as np
import re
import json
import os
import rdflib


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

    sample_query = st.selectbox('Select a sample query', ['All', 'Player belongs to which team?'])
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
            SELECT ?person ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/belong_to_team> ?object.
                ?object <http://schema.org/name> ?object_name
            }
            """
            for row in g.query(query):
                st.write(row)
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
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/ends_with> ?object.
                
            }
            """
            for row in g.query(query):
                st.write(row)
        if predicate == 'has_player_played':
            query = """
            SELECT ?person ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/has_player_played> ?object.
                ?object <http://schema.org/name> ?object_name
                
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
            SELECT ?person ?object
            WHERE {
                ?person <http://dsci558.org/ontology/plays_at> ?object. 
                
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
            SELECT ?person ?object_name
            WHERE {
                ?person <http://dsci558.org/ontology/has_player> ?object.
                ?object <http://schema.org/name> ?object_name
                
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
    path='../'
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


























#run the app
if __name__ == "__main__":
    main()