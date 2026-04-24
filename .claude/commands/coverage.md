Run tests and generate a JaCoCo HTML coverage report.

```bash
./mvnw verify
```

After the build, extract the totals from the HTML report:

```powershell
$html = Get-Content 'target/site/jacoco/index.html' -Raw
$rows = [regex]::Matches($html, '<tfoot>(.*?)</tfoot>', 'Singleline')
$text = $rows[0].Value -replace '<[^>]+>', ' ' -replace '\s+', ' '
Write-Output $text
```

The report is at `target/site/jacoco/index.html`.
Summarise overall instruction and branch coverage percentages.