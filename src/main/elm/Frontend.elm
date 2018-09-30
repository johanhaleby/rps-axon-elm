module Frontend exposing (gameListDecoder, State(..), GameId, Game, PlayerName, Player(..))

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (field, string, maybe, andThen, succeed, fail)
import Maybe exposing (withDefault)
import Url.Builder as Url

-- MAIN


main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }


-- MODEL

type Player =
  Player String

type alias GameId = String
type alias PlayerName = String


type State
  = Joinable
  | Started
  | Ended

type alias Game =
  { gameId : GameId
  , player1 : Maybe Player
  , player2 : Maybe Player
  , winner : Maybe Player
  , state : State}

type Move
  = ROCK
  | PAPER
  | SCISSORS

type Msg
  = AllGames (Result Http.Error (List Game))
  | GetAllGames

type Action
  = StartGame
  | JoinGame


-- INIT

init : () -> (List Game, Cmd Msg)
init _ =
  ( []
  , getAllGames)

-- UPDATE

update : Msg -> List Game -> (List Game, Cmd Msg)
update msg model =
  case msg of
    GetAllGames ->
          (model
           , getAllGames)
    AllGames allGames ->
      case allGames of
        Ok games ->
          ( games
          , Cmd.none
          )

        Err _ ->
          ( model
          , Cmd.none
          )


-- SUBSCRIPTIONS


subscriptions : List Game -> Sub Msg
subscriptions model =
  Sub.none



-- VIEW


playerName : Maybe Player -> String -> String
playerName maybePlayer notFound =
     case maybePlayer of
        Just (Player player) ->
            player
        Nothing -> notFound

renderGame : Game -> Html msg
renderGame game =
    div []
    [h2 [] [ text <| game.gameId]
    , div [] [ text <| playerName game.player1 "Looking for player" ]
    , div [] [ text <| playerName game.player2 "Looking for player" ]
    , div [] [ text <| playerName game.winner "Undecided" ]
    , div [] [ text <| Debug.toString game.state ]
    ]

view : List Game -> Html Msg
view games =
  div []
    [ h2 [] [ text "Welcome to RPS" ]
    , button [ onClick GetAllGames ] [ text "Get all games!" ]
    , div [] [ text <| Debug.toString (List.length games) ]
    , ul [] (List.map (\ikk -> Html.text (Debug.toString ikk)) games)
    , ul [] (List.map renderGame games)
    , br [] []
    , textarea [ cols 40, rows 10 ] [ text "hello" ]
    ]

-- HTTP

getAllGames : Cmd Msg
getAllGames =
  Http.send AllGames (Http.get getAllGamesUrl gameListDecoder)

getAllGamesUrl : String
getAllGamesUrl =
  Url.absolute ["api", "games"] []

gameStateDecoder : Decode.Decoder State
gameStateDecoder =
  string
        |> andThen (\stateAsString ->
           case stateAsString of
                "joinable" ->
                    succeed Joinable
                "ended" ->
                    succeed Ended
                "started" ->
                    succeed Started
                unknown ->
                    fail <| "Unknown game state: " ++ unknown
        )


playerDecoder : Decode.Decoder (Maybe Player)
playerDecoder =
    (maybe string)
        |> andThen (\maybePlayerString ->
            succeed
              <| case maybePlayerString of
                Just player ->
                   Just (Player player)
                _ ->
                   Nothing
        )

gameListDecoder : Decode.Decoder (List Game)
gameListDecoder =
  Decode.list gameDecoder

gameDecoder : Decode.Decoder Game
gameDecoder =
   Decode.map5 Game
        (field "gameId" string)
        (field "player1" playerDecoder)
        (field "player2" playerDecoder)
        (field "winner" playerDecoder)
        (field "state" gameStateDecoder)