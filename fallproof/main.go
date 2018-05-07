package main

import (
	"net/http"

	"github.com/jguer/fallproof/handler"
	"github.com/labstack/echo"
	"github.com/labstack/echo/middleware"
	"github.com/labstack/gommon/log"
	"gopkg.in/mgo.v2"
)

func main() {
	e := echo.New()
	e.Logger.SetLevel(log.ERROR)
	e.Use(middleware.Logger())

	// Database connection
	db, err := mgo.Dial("localhost")
	if err != nil {
		e.Logger.Fatal(err)
	}
	db.SetMode(mgo.Monotonic, true)
	defer db.Close()

	// Initialize handler
	h := &handler.Handler{DB: db}

	// Routes
	e.GET("/", func(c echo.Context) error {
		return c.String(http.StatusOK, "just works")
	})
	e.GET("/register/:user/:id", h.RegisterUser)
	e.GET("/info/:user", h.Info)
	e.POST("/info/:id", h.Data)

	// Start server
	e.Logger.Fatal(e.Start(":1323"))
}
