module Frontend exposing (gameListDecoder, State(..), GameId, Game, PlayerName, Player(..))

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode as Decode exposing (field, string, maybe, andThen, succeed, fail)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)
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
      [ div [class "row"] [
         h2 [class "col-sm-12"] [ text <| game.gameId]
        ]
        , div [class "row"] [
            div [class "col-sm-4"] [ text <| playerName game.player1 "Looking for player" ]
          , div [class "col-sm-4"] [ text <| playerName game.player2 "Looking for player" ]
          , div [class "col-sm-4"] [ text <| playerName game.winner "Undecided" ]
        ]
    ]


stylesheet : String -> Html msg
stylesheet href =
    let
        tag = "link"
        attrs =
            [ attribute "rel"       "stylesheet"
            , attribute "property"  "stylesheet"
            , attribute "href"      href
            ]
        children = []
    in
        node tag attrs children

view : List Game -> Html Msg
view games =
    let
        inner = div [id "inner", class "container"]
                 [ h1 [class "text-center"] [text "hello flash of unstyled content"]
                 , h4 [] [ text "Your Name" ]
                 , input [ attribute "autofocus" "", class "form-control", id "playerName", attribute "minlength" "2", placeholder "Enter your name to play", attribute "required" "", attribute "tabindex" "1", type_ "text" ] []
                 , h2 [] [ text "Welcome to RPS" ]
                 , button [ onClick GetAllGames ] [ text "Get all games!" ]
                 , ul [] (List.map renderGame games)
                 , br [] []
                 , textarea [ cols 40, rows 10 ] [ text "hello" ]
                 ]
        hero = div [id "hero", class "jumbotron"] [inner]
    in
      div [id "outer"]
        [ (stylesheet "//maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css")
        , (stylesheet "/custom.css")
        , (stylesheet "https://unpkg.com/purecss@1.0.0/build/pure-min.css")
        , hero
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
    string |> andThen
        (\player ->
            succeed <| Just <| Player player)

gameListDecoder : Decode.Decoder (List Game)
gameListDecoder =
  Decode.list gameDecoder

gameDecoder : Decode.Decoder Game
gameDecoder =
   Decode.succeed Game
        |> required "gameId" string
        |> optional "player1" playerDecoder Nothing
        |> optional "player2" playerDecoder Nothing
        |> optional "winner" playerDecoder Nothing
        |> required "state" gameStateDecoder