package handler

import (
	"fmt"
	"net/http"

	"github.com/jguer/fallproof/model"
	"github.com/labstack/echo"
	mgo "gopkg.in/mgo.v2"
	"gopkg.in/mgo.v2/bson"
)

func (h *Handler) RegisterUser(c echo.Context) (err error) {
	db := h.DB.Clone()
	defer db.Close()

	// Find User
	u := new(model.User)
	u.Device = c.Param("id")
	u.User = c.Param("user")
	// Validate
	if u.User == "" || u.Device == "" {
		return &echo.HTTPError{Code: http.StatusBadRequest, Message: "invalid user or device ID"}
	}

	if i, err := db.DB("fallproof").C("users").Find(bson.M{"user": u.User}).Count(); i == 0 {
		u.ID = bson.NewObjectId()
		if err = db.DB("fallproof").C("users").Insert(u); err != nil {
			return err
		}
	} else {
		selector := bson.M{"user": u.User}
		updator := bson.M{"$set": bson.M{"device": u.Device}}
		if err := db.DB("fallproof").C("users").Update(selector, updator); err != nil {
			return err
		}
	}

	return c.JSON(http.StatusCreated, u)
}

func (h *Handler) Info(c echo.Context) error {
	userQ := c.Param("user")
	if userQ == "" {
		return &echo.HTTPError{Code: http.StatusBadRequest, Message: "invalid user"}
	}

	// Find User
	u := new(model.User)
	db := h.DB.Clone()
	defer db.Close()
	if err := db.DB("fallproof").C("users").Find(bson.M{"user": userQ}).One(u); err != nil {
		if err == mgo.ErrNotFound {
			return &echo.HTTPError{Code: http.StatusUnauthorized, Message: "invalid user"}
		}
		return err
	}

	// Find Device
	d := new(model.Device)
	if err := db.DB("fallproof").C("devices").Find(bson.M{"device": u.Device}).One(d); err != nil {
		if err == mgo.ErrNotFound {
			return &echo.HTTPError{Code: http.StatusUnauthorized, Message: "invalid user"}
		}
		return err
	}

	return c.JSON(http.StatusOK, d)
}

func (h *Handler) Data(c echo.Context) error {
	db := h.DB.Clone()
	defer db.Close()

	d := new(model.Device)
	if err := c.Bind(d); err != nil {
		fmt.Println(err)
		return err
	}

	if d.Device == "" {
		return &echo.HTTPError{Code: http.StatusBadRequest, Message: "invalid user or device ID"}
	}

	if i, err := db.DB("fallproof").C("devices").Find(bson.M{"device": d.Device}).Count(); i == 0 {
		d.ID = bson.NewObjectId()
		if err = db.DB("fallproof").C("devices").Insert(d); err != nil {
			return err
		}
	} else {
		selector := bson.M{"device": d.Device}
		updator := bson.M{"$set": bson.M{"ainfo": d.AInfo, "time": d.Time, "fell": d.Fell, "emergency": d.Emergency}}
		if err := db.DB("fallproof").C("devices").Update(selector, updator); err != nil {
			return err
		}
	}

	return c.JSON(http.StatusCreated, d)
}
