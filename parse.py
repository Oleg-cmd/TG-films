import requests
import json
import time

TMDB_API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyNTcyNjRhZTcxN2I2NzVkY2ZlZDQxYjMzMDY5ZjE5MSIsIm5iZiI6MTczNjgwMDAwOS4zMTIsInN1YiI6IjY3ODU3NzA5YWJhYmJiYTA0MGJiODQ0YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.vkpelVQyr3EWQBGvAf39e9dOvB-fHcJAiD6une4pw-U"
MAX_RETRIES = 3  # Максимальное количество повторных попыток
RETRY_DELAY = 2  # Задержка между попытками в секундах
MAX_MOVIES = 200 #
OMDB_API_KEY = "ce97e76d"



def get_movie_data(movie_title):
    url = f"http://www.omdbapi.com/"
    params = {
        "apikey": OMDB_API_KEY,
        "t": movie_title,
        "r": "json",
        "plot": "full",
        "lang": "ru"
    }
    retries = 0
    while retries < MAX_RETRIES:
        try:
            response = requests.get(url, params=params, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            retries += 1
            print(f"Error fetching movie '{movie_title}': {e}. Retrying in {RETRY_DELAY} seconds (Attempt {retries}/{MAX_RETRIES})")
            time.sleep(RETRY_DELAY)
    print(f"Max retries exceeded for movie '{movie_title}', skipping.")
    return None


def get_tmdb_movie_list(page=1):
    url = f"https://api.themoviedb.org/3/movie/popular?page={page}"
    headers = {
        "accept": "application/json",
        "Authorization": f"Bearer {TMDB_API_KEY}"
    }
    retries = 0
    while retries < MAX_RETRIES:
        try:
            response = requests.get(url, headers=headers, timeout=10)
            response.raise_for_status()
            return response.json()
        except requests.exceptions.RequestException as e:
            retries += 1
            print(f"Error fetching TMDb movie list: {e}. Retrying in {RETRY_DELAY} seconds (Attempt {retries}/{MAX_RETRIES})")
            time.sleep(RETRY_DELAY)
    print("Max retries exceeded for TMDb movie list.")
    return None


def save_movies_to_json(movies_data, filepath="movies.json"):
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(movies_data, f, indent=4, ensure_ascii=False)


if __name__ == "__main__":
    movies = []
    movie_count = 0
    page = 1

    try:
        while movie_count < MAX_MOVIES:
            tmdb_data = get_tmdb_movie_list(page)
            if not tmdb_data:
                break
            for movie in tmdb_data["results"]:
                if movie_count >= MAX_MOVIES:
                    break
                movie_title = movie["title"]
                movie_detail = get_movie_data(movie_title)
                if movie_detail and movie_detail.get("Response") == "True":
                    try:
                        genres = movie_detail["Genre"].split(", ") if movie_detail.get("Genre") else []
                        release_year = int(movie_detail["Year"]) if movie_detail.get("Year") else None
                        movies.append({
                            "title": movie_detail["Title"],
                            "description": movie_detail["Plot"],
                            "release_year": release_year,
                            "external_rating": float(movie_detail["imdbRating"]) if movie_detail.get("imdbRating") != "N/A" else None,
                            "poster_url": movie_detail["Poster"] if movie_detail.get("Poster") != "N/A" else None,
                            "genres": genres
                        })
                        movie_count += 1
                    except Exception as e:
                        print(f"Error processing movie '{movie_title}': {e}")
            page += 1
            print(f"Loaded {movie_count} movies.")
        save_movies_to_json(movies)
        print(f"Finished processing {movie_count} movies. Data saved.")
    except Exception as e:
        print(f"An unexpected error occurred: {e}. Saving current data...")
        save_movies_to_json(movies)
    finally:
        save_movies_to_json(movies)
