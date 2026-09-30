// Allure 3 report configuration (read automatically by `npx allure generate`)
export default {
  name: "Books API Tests",
  // Trend history across runs: one line per run, pruned to the last 20 runs
  historyPath: "./allure-history/history.jsonl",
  appendHistory: true,
  historyLimit: 20,
};
