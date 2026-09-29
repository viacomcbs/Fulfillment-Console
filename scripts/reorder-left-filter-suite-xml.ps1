# Reorder left-filter per-class tests in suite XMLs to:
# Basic -> Option order -> Scroll -> Search -> Table sync -> Active filters -> Clear -> Select all
$ErrorActionPreference = 'Stop'
$root = Join-Path $PSScriptRoot '..\src\test\resources\regression\left-filters'
$suffixOrder = [ordered]@{
    'BasicTest'         = 1
    'OptionOrderTest'   = 2
    'ScrollTest'        = 3
    'SearchTest'        = 4
    'TableSyncTest'     = 5
    'ActiveFiltersTest' = 6
    'ClearFiltersTest'  = 7
    'SelectAllTest'     = 8
    'EnterRangeTest'    = 9
}

function Get-CategoryRank([string]$className) {
    foreach ($suffix in $suffixOrder.Keys) {
        if ($className -like "*_$suffix") { return $suffixOrder[$suffix] }
    }
    return 999
}

$files = Get-ChildItem -Path $root -Recurse -Filter '*ProdServerSuite.xml'
$updated = 0
foreach ($file in $files) {
    [xml]$doc = Get-Content -LiteralPath $file.FullName -Encoding UTF8
    $changed = $false
    foreach ($test in @($doc.suite.test)) {
        $classesNode = $test.SelectSingleNode('classes')
        if ($null -eq $classesNode) { continue }
        $classes = @($classesNode.SelectNodes('class'))
        if ($classes.Count -lt 2) { continue }
        $lfClasses = $classes | Where-Object { $_.GetAttribute('name') -match 'leftfiltersvalidation\.perfilter\.' }
        if ($lfClasses.Count -lt 2) { continue }

        $indexed = for ($i = 0; $i -lt $classes.Count; $i++) {
            $name = $classes[$i].GetAttribute('name')
            [pscustomobject]@{ Node = $classes[$i]; Rank = (Get-CategoryRank $name); Index = $i; Name = $name }
        }
        $sorted = $indexed | Sort-Object Rank, Index
        $original = ($indexed | ForEach-Object { $_.Name }) -join '|'
        $newOrder = ($sorted | ForEach-Object { $_.Name }) -join '|'
        if ($original -eq $newOrder) { continue }

        $cloned = foreach ($item in $sorted) { $item.Node.CloneNode($true) }
        while ($classesNode.ChildNodes.Count -gt 0) {
            [void]$classesNode.RemoveChild($classesNode.FirstChild)
        }
        foreach ($node in $cloned) {
            [void]$classesNode.AppendChild($node)
        }
        $changed = $true
    }
    if ($changed) {
        $doc.Save($file.FullName)
        $updated++
        Write-Host "Updated: $($file.FullName)"
    }
}
Write-Host "Done. Updated $updated suite file(s)."
