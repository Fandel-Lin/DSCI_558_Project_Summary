import streamlit as st
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import json


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

    #First Page
    if page == "Homepage":
        homepage()
#     #Search Page
#     if page == "Search":
#         Search()
#     #Predict Page
#     if page == "Predict":
#         Predict()
#     #Player Information Page
#     if page == "Player Information":
#         Player()

        
        
        
        
def homepage():
    st.write("""
            # MLB KG
            #### This project is about to build a knowledge graph for MLB.
            ###### This app was created by _**Zihao H., Fandel Lin.**_
            #""")


  

