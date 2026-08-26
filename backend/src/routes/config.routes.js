const express = require("express");
const { TOPIC_TAGS, SUPPORTED_LANGUAGES } = require("../config/app-options");

const router = express.Router();

router.get("/tags", (req, res) => {
    res.status(200).json({
        tags: TOPIC_TAGS
    });
});

router.get("/languages", (req, res) => {
    res.status(200).json({
        languages: SUPPORTED_LANGUAGES
    });
});

module.exports = router;
