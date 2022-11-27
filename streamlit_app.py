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
            "Search",
            "Predict",
            "Player Information",
            "Scheduel"

        ]
    )

    # First Page
    if page == "Homepage":
        homepage()


    #Search Page
    if page == "Search":
        Search()
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
    sparql = """ {} """.format(st.text_input("Enter a query"))

    g = rdflib.Graph()
    g.parse('prototype.ttl', format="ttl")

    if sparql !=""" {} """.format(''):
        for row in g.query(sparql):
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